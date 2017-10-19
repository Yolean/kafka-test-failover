package se.yolean.kafka.test.failover.analytics;

import java.util.LinkedList;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Timer;
import se.yolean.kafka.test.failover.ConsistencyFatalError;
import se.yolean.kafka.test.failover.RunId;

public class TestMessageLogImpl extends LinkedList<TestMessage> implements TestMessageLog {

	private static final long serialVersionUID = 1L;

	private ILogger log = SLoggerFactory.getLogger(this.getClass());

	static final Gauge unseenSentMessages = Gauge.build().name("unseen_sent_messages")
			.help("Messages created that haven't been seen consumed").register();

	static final Gauge produceAckLatency = Gauge.build().name("produce_ack_latency")
			.help("The time it took to get last ack").register();

	static final Gauge produceConsumeLatency = Gauge.build().name("produce_consume_latency")
			.help("The time it took from send to receive of last successful message").register();

	static final Gauge pendingMessages = Gauge.build().name("pending_messages")
			.help("Messages produced but not yet consumed").register();
	
	private final RunId runId;

	int lastCreate = -1;
	Timer lastTimer = null;

	public TestMessageLogImpl(RunId runId) {
		this.runId = runId;
	}

	public TestMessage createNext(int i) {
		return new TestMessage(runId, i);
	}

	@Override
	public ProducerRecord<String, String> createNext(int i, String topic) {
		if (this.size() != i) {
			throw new IllegalStateException("Can't happen, right?");
		}
		TestMessage msg = createNext(i);
		this.add(msg);
		unseenSentMessages.inc();
		lastCreate = i;
		lastTimer = produceAckLatency.startTimer();
		return new ProducerRecord<String, String>(topic, msg.getKey(), msg.getMessage());
	}

	@Override
	public void onProducerAckReceived(int i, RecordMetadata metadata) {
		if (lastCreate != i) {
			log.error("Producer ack(s) missing?", "last", lastCreate, "got", i, "offset", metadata.offset(),
					"timestamp", metadata.timestamp(), "lastTimestamp", metadata.timestamp());
			throw new ConsistencyFatalError("Expected an ack for every message");
		}
		lastTimer.setDuration();
		this.get(i).setProduced(metadata.partition(), metadata.offset(), metadata.timestamp());
	}

	@Override
	public void onConsumed(ConsumerRecord<String, String> r) {
		String key = r.key();
		if (!TestMessage.isSameRun(runId, key)) {
			log.trace("Ignoring message not from this run", "offset", r.offset());
			return;
		}
		unseenSentMessages.dec();
	}

}
