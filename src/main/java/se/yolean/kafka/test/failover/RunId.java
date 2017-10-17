package se.yolean.kafka.test.failover;

import org.apache.commons.text.RandomStringGenerator;

public class RunId {

	private static final int LENGTH = 5;
	
	private static RandomStringGenerator random;
	
	static {
		random = new RandomStringGenerator.Builder().withinRange('a', 'z').build();
	}
	
	private final String id;
	
	public RunId() {
		id = random.generate(LENGTH);
	}

	@Override
	public boolean equals(Object id) {
		return id != null && toString().equals(id.toString());
	}

	@Override
	public String toString() {
		return id;
	}
	
}
