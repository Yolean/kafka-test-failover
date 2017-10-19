package se.yolean.kafka.test.failover;

import static org.junit.Assert.*;

import org.junit.Test;

public class RunIdTest {

	@Test
	public void testToString() {
		RunId id1 = new RunId();
		assertNotNull(id1.toString());
		assertFalse(id1.toString().contains(" "));
		assertTrue(id1.toString().length() > 4);
	}

	@Test
	public void testAppId() {
		RunId id2 = new RunId("TEST");
		assertTrue(id2.toString().startsWith("TEST"));
	}

}
