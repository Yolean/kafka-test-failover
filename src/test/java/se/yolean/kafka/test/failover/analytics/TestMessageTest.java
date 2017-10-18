package se.yolean.kafka.test.failover.analytics;

import static org.junit.Assert.assertEquals;
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
		assertEquals(runId.toString() + 0, msg.getKey());
		String message = msg.getMessage();
		Date created = TestMessage.getMessageCreated(message);
		long diff = created.getTime() - testTime.getTime();
		assertTrue("test=" + testTime.getTime() + ",message=" + created.getTime() + ",diff=" + diff, diff < 100);
		assertTrue(diff >= 0);
	}

}
