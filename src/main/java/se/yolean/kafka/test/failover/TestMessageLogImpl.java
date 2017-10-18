package se.yolean.kafka.test.failover;

import java.util.LinkedList;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class TestMessageLogImpl
	extends LinkedList<TestMessage>
	implements TestMessageLog {
	
	private static final long serialVersionUID = 1L;
	
	private ILogger log = SLoggerFactory.getLogger(this.getClass());

	public TestMessage createNext(RunId runId, int i) {
		return new TestMessage(runId, i);
	}

	@Override
	public ProducerRecord<String, String> createNext(RunId runId, int i, String topic) {
		TestMessage msg = createNext(runId, i);
		this.add(msg);
		return new ProducerRecord<String, String>(topic, msg.getKey(), msg.getMessage());
	}

	@Override
	public void onProducerAckReceived(RecordMetadata metadata) {
		log.debug("TODO verify produced");
	}

	@Override
	public void onConsumed(ConsumerRecord<String, String> r) {
		log.debug("TODO verify consumed");
	}

	@Override
	public void onIntervalInsufficient(int i, long actualLoopDuration, int targetInterval) {
		log.warn("Interval insufficient", "index", i, "duration", actualLoopDuration, "target", targetInterval);
	}
	
}
