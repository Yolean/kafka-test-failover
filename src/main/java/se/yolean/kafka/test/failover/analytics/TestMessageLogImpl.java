package se.yolean.kafka.test.failover.analytics;

import java.util.LinkedList;

import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import io.prometheus.client.Gauge;
import se.yolean.kafka.test.failover.RunId;

public class TestMessageLogImpl
	extends LinkedList<TestMessage>
	implements TestMessageLog {
	
	private static final long serialVersionUID = 1L;
	
	private ILogger log = SLoggerFactory.getLogger(this.getClass());
	
	final Gauge unseenSentMessages = Gauge.build().name("unseen_sent_messages")
			.help("Messages created that haven't been seen consumed").register();

	final Gauge unseenAckdMessages = Gauge.build().name("unseen_ackd_messages")
			.help("Messaced produced+acked that haven't been seen consumed").register();	
	
	public TestMessage createNext(RunId runId, int i) {
		return new TestMessage(runId, i);
	}

	@Override
	public ProducerRecord<String, String> createNext(RunId runId, int i, String topic) {
		TestMessage msg = createNext(runId, i);
		this.add(msg);
		unseenSentMessages.inc();
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
	public void onIntervalInsufficient(int i, long durationPrevious, int targetIntervalMs) {
		log.warn("Interval insufficient", "index", i, "duration", durationPrevious, "target", targetIntervalMs);
	}
	
}
