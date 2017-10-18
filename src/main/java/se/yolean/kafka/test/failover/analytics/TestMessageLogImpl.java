package se.yolean.kafka.test.failover.analytics;

import java.util.LinkedList;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import io.prometheus.client.Gauge;
import se.yolean.kafka.test.failover.ConsistencyFatalError;
import se.yolean.kafka.test.failover.RunId;

public class TestMessageLogImpl extends LinkedList<TestMessage> implements TestMessageLog {

	private static final long serialVersionUID = 1L;

	private ILogger log = SLoggerFactory.getLogger(this.getClass());

	final Gauge unseenSentMessages = Gauge.build().name("unseen_sent_messages")
			.help("Messages created that haven't been seen consumed").register();

	final Gauge unseenAckdMessages = Gauge.build().name("unseen_ackd_messages")
			.help("Messaced produced+acked that haven't been seen consumed").register();

	private RecordMetadata lastProducerAck = null;

	private final RunId runId;

	public TestMessageLogImpl(RunId runId) {
		this.runId = runId;
	}

	public TestMessage createNext(int i) {
		return new TestMessage(runId, i);
	}

	@Override
	public ProducerRecord<String, String> createNext(int i, String topic) {
		TestMessage msg = createNext(i);
		this.add(msg);
		unseenSentMessages.inc();
		return new ProducerRecord<String, String>(topic, msg.getKey(), msg.getMessage());
	}

	/**
	 * @deprecated Needs work to support multiple concurrent runs
	 */
	@Override
	public void onProducerAckReceived(RecordMetadata metadata) {
		unseenAckdMessages.inc();
		if (lastProducerAck != null && metadata.offset() - 1 != lastProducerAck.offset()) {
			log.error("Producer ack(s) missing?", "offset", metadata.offset(), "lastOffset", lastProducerAck.offset(),
					"timestamp", metadata.timestamp(), "lastTimestamp", metadata.timestamp());
			throw new ConsistencyFatalError("Expected an ack for every message");
		}
		lastProducerAck = metadata;
	}

	@Override
	public void onConsumed(ConsumerRecord<String, String> r) {
		String key = r.key();
		if (!TestMessage.isSameRun(runId, key)) {
			log.trace("Ignoring message not from this run", "offset", r.offset());
			return;
		}
		unseenSentMessages.dec();
		unseenAckdMessages.dec();
	}

	@Override
	public void onIntervalInsufficient(int i, long durationPrevious, int targetIntervalMs) {
		log.warn("Interval insufficient", "index", i, "duration", durationPrevious, "target", targetIntervalMs);
	}

}
