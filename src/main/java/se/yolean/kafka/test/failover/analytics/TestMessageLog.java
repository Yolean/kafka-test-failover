package se.yolean.kafka.test.failover.analytics;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import se.yolean.kafka.test.failover.ConsistencyFatalError;

/**
 * Doesn't communicate with Kafka, but understands the API enough to verify
 * message consistency.
 */
public interface TestMessageLog extends Iterable<TestMessage> {

	// TestMessage createNext(int i);

	ProducerRecord<String, String> createNext(int i, String topic);

	/**
	 * @throws ConsistencyFatalError
	 *             On consistencies that are too odd/big to be represented by
	 *             metrics
	 */
	void onProducerAckReceived(RecordMetadata recordMetadata);

	/**
	 * @throws ConsistencyFatalError
	 *             On consistencies that are too odd/big to be represented by
	 *             metrics
	 */
	void onConsumed(ConsumerRecord<String, String> r);

	void onIntervalInsufficient(int i, long durationPrevious, int targetIntervalMs);

}