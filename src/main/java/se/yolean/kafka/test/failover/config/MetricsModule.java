package se.yolean.kafka.test.failover.config;

import org.apache.kafka.common.metrics.Metrics;

import com.google.inject.AbstractModule;

import se.yolean.kafka.test.failover.TestMessageLog;
import se.yolean.kafka.test.failover.metrics.TestMessageLogImpl;

public class MetricsModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Metrics.class).asEagerSingleton();
		
		bind(TestMessageLog.class).to(TestMessageLogImpl.class);
	}

}
