package se.yolean.kafka.test.failover.config;

import com.google.inject.AbstractModule;

import se.yolean.kafka.test.failover.ProducerConsumerRun;
import se.yolean.kafka.test.failover.analytics.TestMessageLog;
import se.yolean.kafka.test.failover.analytics.TestMessageLogImpl;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ProducerConsumerRun.class);

		bind(TestMessageLog.class).to(TestMessageLogImpl.class);
	}

}
