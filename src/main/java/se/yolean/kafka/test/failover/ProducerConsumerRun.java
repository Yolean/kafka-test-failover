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
	@Named("producerDefaults")
	private Properties producerProps;

	@Inject
	@Named("consumerDefaults")
	private Properties consumerProps;

	@Inject
	@Named("config:topic")
	private String topic;

	public void start() {
		log.info("Starting", "topic", topic, "bootstrap", producerProps.getProperty("bootstrap.servers"));

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);
		consumer.subscribe(Arrays.asList(topic));

		Producer<String, String> producer = new KafkaProducer<>(producerProps);
		for (int i = 0; i < 10; i++) {
			log.debug("Producing", "message", i);
			ProducerRecord<String, String> record = new ProducerRecord<String, String>(topic, Integer.toString(i),
					"msg" + Integer.toString(i));
			Future<RecordMetadata> producing = producer.send(record);
			waitForAck(producing);

			ConsumerRecords<String, String> consumed = consumer.poll(100);
			for (ConsumerRecord<String, String> r : consumed) {
				log.info("consumed", "offset", r.offset(), "timestamp", r.timestamp(), "key", r.key(), "value",
						r.value());
			}
		}
		producer.close();
		consumer.close();
	}

	private void waitForAck(Future<RecordMetadata> producing) {
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
		}
	}

}
