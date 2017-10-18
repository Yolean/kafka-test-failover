package se.yolean.kafka.test.failover;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import se.yolean.kafka.test.failover.analytics.TestMessageLog;
import se.yolean.kafka.test.failover.analytics.TestMessageLogImpl;

public class ProducerConsumerRun {

	private ILogger log = SLoggerFactory.getLogger(this.getClass());

	@Inject
	@Named("config:messagesMax")
	private int messagesMax;

	@Inject
	@Named("config:messageIntervalMs")
	private int messageIntervalMs;

	@Inject
	@Named("producerDefaults")
	private Properties producerProps;

	@Inject
	@Named("consumerDefaults")
	private Properties consumerProps;

	@Inject
	@Named("config:topic")
	private String topic;

	public final Histogram iterationLatency = Histogram.build().name("iteration_latency_ms")
			.help("Time taken for each test loop, excluding initial wait").register();

	final Counter iterations = Counter.build().name("iterations").help("Test loop iterations started so far")
			.register();

	/**
	 * @param runId
	 *            Unique, in case runs share a topic
	 * @throws ConsistencyFatalError
	 *             On consistencies that are too odd/big to be represented by
	 *             metrics
	 */
	public void start(RunId runId) throws ConsistencyFatalError {
		log.info("Starting", "runId", runId, "topic", topic, "bootstrap",
				producerProps.getProperty("bootstrap.servers"));

		TestMessageLog messageLog = new TestMessageLogImpl(runId);
		
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
		Producer<String, String> producer = new KafkaProducer<>(producerProps);
		
		try {
			start(messageLog, consumer, producer);
		} finally {
			producer.close();
			consumer.close();
		}
	}

	void start(TestMessageLog messageLog, KafkaConsumer<String, String> consumer, Producer<String, String> producer) {

		AlwaysSeekToEndListener<String, String> rebalanceListner = new AlwaysSeekToEndListener<>(consumer);
		consumer.subscribe(Arrays.asList(topic), rebalanceListner);

		long t = System.currentTimeMillis();
		for (int i = 0; i < messagesMax; i++) {
			iterations.inc();
			long durationPrevious = System.currentTimeMillis() - t;
			long wait = messageIntervalMs - durationPrevious;
			if (wait > 0) {
				try {
					Thread.sleep(wait);
				} catch (InterruptedException e) {
					throw new RuntimeException("Got aborted at wait", e);
				}
			} else {
				messageLog.onIntervalInsufficient(i - 1, durationPrevious, messageIntervalMs);
			}

			t = System.currentTimeMillis();
			Histogram.Timer iterationTimer = iterationLatency.startTimer();
			try {
				ProducerRecord<String, String> record = messageLog.createNext(i, topic);
				log.debug("Producer send", "key", record.key(), "afterWait", wait);
				Future<RecordMetadata> producing = producer.send(record);
				RecordMetadata metadata = waitForAck(producing);
				log.debug("Got producer ack", "topic", metadata.topic(), "partition", metadata.partition(), "offset",
						metadata.offset(), "timestamp", metadata.timestamp(), metadata, "keySize",
						metadata.serializedKeySize(), "valueSize", metadata.serializedValueSize());
				messageLog.onProducerAckReceived(metadata);

				ConsumerRecords<String, String> consumed = consumer.poll(100);
				for (ConsumerRecord<String, String> r : consumed) {
					log.info("consumed", "offset", r.offset(), "timestamp", r.timestamp(), "key", r.key(), "value",
							r.value());
					messageLog.onConsumed(r);
				}
			} finally {
				iterationTimer.observeDuration();
			}
		}
	}

	private RecordMetadata waitForAck(Future<RecordMetadata> producing) {
		int timeout = 100;
		// block while sending
		final RecordMetadata metadata;
		try {
			metadata = producing.get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.error("Got interrupted (probably not by Kafka) while waiting for ack", e);
			throw new ConsistencyFatalError(e);
		} catch (ExecutionException e) {
			log.error("Something must have gone wrong while producing", e);
			throw new ConsistencyFatalError(e);
		} catch (TimeoutException e) {
			log.error("Failed to get an ack within", "milliseconds", timeout, e);
			throw new ConsistencyFatalError(e);
		}
		if (metadata == null) {
			throw new RuntimeException("Failed with reason unkown to get ack for message " + producing);
		}
		return metadata;
	}

}
