package se.yolean.kafka.test.failover;

import org.apache.commons.text.RandomStringGenerator;

public class RunId {

	public static final int LENGTH = 9;

	private static RandomStringGenerator random;

	static {
		random = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
	}

	private final String keyPrefix;

	private final String id;

	public RunId() {
		this("");
	}

	public RunId(String keyPrefix) {
		this.keyPrefix = keyPrefix;
		id = random.generate(LENGTH);
	}

	@Override
	public boolean equals(Object id) {
		return id != null && toString().equals(id.toString());
	}

	@Override
	public String toString() {
		return keyPrefix + id;
	}

}
