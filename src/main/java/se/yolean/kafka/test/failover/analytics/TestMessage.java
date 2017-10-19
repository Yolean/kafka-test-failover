package se.yolean.kafka.test.failover.analytics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import io.prometheus.client.Histogram;
import se.yolean.kafka.test.failover.RunId;

/**
 * Facilitates consistency checks, even if there are multiple concurrent runs.
 */
public class TestMessage {

	public static final int LENGTH = 9;

	private static final String MSG_COUNTER_FORMAT = "%0" + LENGTH + "d";

	private static final String MSG_ATTRIBUTE_SEPARATOR = "/";

	private static final DateFormat FORMAT_HUMAN_READABLE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	static {
		FORMAT_HUMAN_READABLE.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	static final Histogram timestampDiff = Histogram.build().name("timestamp_diff")
			.help("Time between the local timestamp at message create and kafka's timestamp on the same message (ms)")
			.register();

	private RunId runId;
	private int i;
	private Date created;

	TestMessage(RunId runId, int i) {
		this(new Date(), runId, i);
	}

	protected TestMessage(Date created, RunId runId, int i) {
		this.created = new Date();
		this.runId = runId;
		this.i = i;
	}

	public String getKey() {
		return runId.toString() + String.format(MSG_COUNTER_FORMAT, i);
	}

	@Override
	public boolean equals(Object msg) {
		return super.equals(msg);
	}

	@Override
	public String toString() {
		return getKey();
	}

	public String getMessage() {
		return created.getTime() + MSG_ATTRIBUTE_SEPARATOR + FORMAT_HUMAN_READABLE.format(created);
	}

	void setProduced(int partition, long offset, long timestamp) {
		timestampDiff.observe(timestamp - created.getTime());
	}

	static Date getMessageCreated(String message) {
		if (message == null) {
			throw new IllegalArgumentException("Got null message");
		}
		int sep = message.indexOf(MSG_ATTRIBUTE_SEPARATOR);
		if (sep < 10) {
			throw new IllegalArgumentException("Unexpected message: " + message);
		}
		String created = message.substring(0, sep);
		long c = Long.parseLong(created);
		return new Date(c);
	}

	static boolean isSameRun(RunId runId, String key) {
		return key != null && key.startsWith(runId.toString());
	}

}
