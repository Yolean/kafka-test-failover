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

	private TestMessageLog messageLog = new TestMessageLogImpl();

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

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
		consumer.subscribe(Arrays.asList(topic));

		Producer<String, String> producer = new KafkaProducer<>(producerProps);

		for (int i = 0; i < messagesMax; i++) {
			ProducerRecord<String, String> record = messageLog.createNext(runId, i, topic);
			log.debug("Producer send", "timestamp", record.key());
			Future<RecordMetadata> producing = producer.send(record);
			RecordMetadata metadata = waitForAck(producing);
			log.debug("Got producer ack", "topic", metadata.topic(), "partition", metadata.partition(), "offset",
					metadata.offset(), "timestamp", metadata.timestamp());
			messageLog.onProducerAckReceived(metadata);

			ConsumerRecords<String, String> consumed = consumer.poll(100);
			for (ConsumerRecord<String, String> r : consumed) {
				log.info("consumed", "offset", r.offset(), "timestamp", r.timestamp(), "key", r.key(), "value",
						r.value());
				messageLog.onConsumed(r);
			}
		}
		producer.close();
		consumer.close();
	}

	private RecordMetadata waitForAck(Future<RecordMetadata> producing) {
		int timeout = 100;
		// block while sending
		final RecordMetadata metadata;
		try {
			metadata = producing.get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			log.error("Got interrupted (probably not by Kafka) while waiting for ack", e);
			throw new AssertionError(e);
		} catch (ExecutionException e) {
			log.error("Something must have gone wrong while producing", e);
			throw new AssertionError(e);
		} catch (TimeoutException e) {
			log.error("Failed to get an ack within", "milliseconds", timeout, e);
			throw new AssertionError(e);
		}
		if (metadata != null) {
			log.info("ack", "offset", metadata.offset(), "partition", metadata.partition(), "keySize",
					metadata.serializedKeySize(), "valueSize", metadata.serializedValueSize());
		} else {
			throw new RuntimeException("Failed to get ack for message " + producing);
		}
		return metadata;
	}

}
