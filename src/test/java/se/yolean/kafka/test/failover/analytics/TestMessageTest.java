package se.yolean.kafka.test.failover.analytics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import se.yolean.kafka.test.failover.RunId;
import se.yolean.kafka.test.failover.analytics.TestMessage;

public class TestMessageTest {

	@Test
	public void test() {
		RunId runId = new RunId();
		Date testTime = new Date();
		TestMessage msg = new TestMessage(runId, 0);
		String message = msg.getMessage();
		Date created = TestMessage.getMessageCreated(message);
		long diff = created.getTime() - testTime.getTime();
		assertTrue("test=" + testTime.getTime() + ",message=" + created.getTime() + ",diff=" + diff, diff < 100);
		assertTrue(diff >= 0);
		assertTrue(TestMessage.isSameRun(runId, msg.getKey()));
		assertFalse(TestMessage.isSameRun(new RunId(), msg.getKey()));
		assertFalse(TestMessage.isSameRun(runId, "abcdef"));
	}

}
