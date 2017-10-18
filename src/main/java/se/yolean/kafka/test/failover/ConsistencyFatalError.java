package se.yolean.kafka.test.failover;

public class ConsistencyFatalError extends AssertionError {

	private static final long serialVersionUID = 1L;

	public ConsistencyFatalError(Exception e) {
		super(e);
	}

	public ConsistencyFatalError(String message) {
		super(message);
	}

}
