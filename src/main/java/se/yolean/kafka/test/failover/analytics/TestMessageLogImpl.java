package se.yolean.kafka.test.failover.analytics;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import io.prometheus.client.Gauge;
import io.prometheus.client.Gauge.Timer;
import se.yolean.kafka.test.failover.ConsistencyFatalError;
import se.yolean.kafka.test.failover.RunId;

public class TestMessageLogImpl implements TestMessageLog {

	private ILogger log = SLoggerFactory.getLogger(this.getClass());

	static final Gauge unseenSentMessages = Gauge.build().name("unseen_sent_messages")
			.help("Messages created that haven't been seen consumed").register();

	static final Gauge produceAckLatency = Gauge.build().name("produce_ack_latency")
			.help("The time it took to get last ack").register();

	static final Gauge produceConsumeLatency = Gauge.build().name("produce_consume_latency")
			.help("The time it took from send to receive of last successful message").register();

	static final Gauge pendingMessages = Gauge.build().name("pending_messages")
			.help("Messages produced but not yet consumed").register();
	
	private Map<Integer,TestMessage> byIndex = new HashMap<Integer,TestMessage>();
	private Map<String,TestMessage> byKey = new HashMap<String,TestMessage>();
	
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
		TestMessage msg = createNext(i);
		byIndex.put(i, msg);
		byKey.put(msg.getKey(), msg);
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
		byIndex.get(i).setProduced(metadata.partition(), metadata.offset(), metadata.timestamp());
		byIndex.remove(i);
	}

	@Override
	public void onConsumed(ConsumerRecord<String, String> r) {
		String key = r.key();
		if (!TestMessage.isSameRun(runId, key)) {
			log.trace("Ignoring message not from this run", "offset", r.offset());
			return;
		}
		unseenSentMessages.dec();
		TestMessage msg = byKey.get(r.key());
		if (msg == null) {
			log.error("Unrecognized message, or message already consumed", "key", r.key(), "offset", r.offset(), "timestamp", r.timestamp());
			return;
		}
		byKey.remove(r.key());
	}

}
