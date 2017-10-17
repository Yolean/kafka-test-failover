package se.yolean.kafka.test.failover;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Doesn't communicate with Kafka, but understands the API to constantly verify consistency.
 */
public interface TestMessageLog extends Iterable<TestMessage> {

	//TestMessage createNext(RunId runId, int i);

	ProducerRecord<String, String> createNext(RunId runId, int i, String topic);	
	
	/**
	 * @throws AssertionError if some consistency assertions fails
	 */
	void onProducerAckReceived(RecordMetadata recordMetadata);

	/**
	 * @throws AssertionError if some consistency assertions fails
	 */
	void onConsumed(ConsumerRecord<String, String> r);

}