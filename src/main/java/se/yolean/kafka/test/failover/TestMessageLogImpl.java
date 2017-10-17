package se.yolean.kafka.test.failover;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class TestMessageLogImpl
	extends LinkedList<TestMessage>
	implements TestMessageLog {
	
	private ILogger log = SLoggerFactory.getLogger(this.getClass());

	public TestMessage createNext(RunId runId, int i) {
		return new TestMessage(runId, i);
	}

	@Override
	public ProducerRecord<String, String> createNext(RunId runId, int i, String topic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onProducerAckReceived(RecordMetadata recordMetadata) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConsumed(ConsumerRecord<String, String> r) {
		// TODO Auto-generated method stub
		
	}
	
}
