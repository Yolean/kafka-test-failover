package se.yolean.kafka.test.failover.config;

import com.google.inject.AbstractModule;

import se.yolean.kafka.test.failover.ProducerConsumerRun;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(ProducerConsumerRun.class);
	}

}
