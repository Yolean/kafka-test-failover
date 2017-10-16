package se.yolean.kafka.test.failover;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.Test;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class KafkaProducerSetupIntegrationTest {

	private ILogger log = SLoggerFactory.getLogger(this.getClass());

	static {
		com.github.structlog4j.StructLog4J.setFormatter(com.github.structlog4j.json.JsonFormatter.getInstance());
	}

	@Test
	public void test() {
		Properties props = new Properties();
		// https://github.com/Yolean/kubernetes-kafka/pull/78
		props.put("bootstrap.servers", "192.168.99.100:32400,192.168.99.100:32401,192.168.99.100:32402");
		props.put("acks", "all");
		//props.put("retries", 3);
		props.put("batch.size", 16384);
		props.put("linger.ms", 1);
		props.put("buffer.memory", 33554432);
		props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
		props.put("request.timeout.ms", 5000);
		//props.put("enable.idempotence", true);

		Producer<String, String> producer = new KafkaProducer<>(props);
		for (int i = 0; i < 10; i++) {
			System.out.println("Producing " + i);
			Future<RecordMetadata> producing = producer.send(new ProducerRecord<String, String>(
					"test-basic-with-kafkacat",
					Integer.toString(i),
					"msg" + Integer.toString(i)));
			waitForAck(producing);
		}

		producer.close();
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
			log.info("ack", "offset", metadata.offset(), "partition", metadata.partition(), "keySize", metadata.serializedKeySize(), "valueSize", metadata.serializedValueSize());
		}
	}

}
