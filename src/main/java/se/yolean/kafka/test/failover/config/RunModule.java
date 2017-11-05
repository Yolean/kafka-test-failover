package se.yolean.kafka.test.failover.config;

import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import se.yolean.kafka.test.failover.ProducerConsumerRun;
import se.yolean.kafka.test.failover.RunId;
import se.yolean.kafka.test.failover.analytics.TestMessage;

public class RunModule extends AbstractModule {

	/**
	 * Depends on ID logic in {@link TestMessage}
	 */
	private static final int DEFAULT_MESSAGES_MAX = 999999999;

	public static final String DEFAULT_BOOTSTRAP_SERVERS = "192.168.99.102:32400,192.168.99.102:32401,192.168.99.102:32402";
	public static final String DEFAULT_TOPIC = "test-produce-consume-latency";

	private RunId runId;

	public RunModule(RunId runId) {
		this.runId = runId;
	}

	@Override
	protected void configure() {
		bind(RunId.class).toInstance(runId);
		
		bind(ProducerConsumerRun.class);

		bind(String.class).annotatedWith(Names.named("config:bootstrap"))
				.toProvider(new ConfigValueProvider.Str("BOOTSTRAP", DEFAULT_BOOTSTRAP_SERVERS));

		bind(String.class).annotatedWith(Names.named("config:topic"))
				.toProvider(new ConfigValueProvider.Str("TOPIC", DEFAULT_TOPIC));

		bind(Integer.class).annotatedWith(Names.named("config:messagesMax"))
				.toProvider(new ConfigValueProvider.Int("MESSAGES_MAX", DEFAULT_MESSAGES_MAX));
		bind(Integer.class).annotatedWith(Names.named("config:messageIntervalMs"))
				.toProvider(new ConfigValueProvider.Int("MESSAGE_INTERVAL", 1000));
		bind(Integer.class).annotatedWith(Names.named("config:ackTimeoutMs"))
				.toProvider(new ConfigValueProvider.Int("ACK_TIMEOUT_MS", 100));
		bind(Integer.class).annotatedWith(Names.named("config:consumerPollMs"))
				.toProvider(new ConfigValueProvider.Int("CONSUMER_POLL_MS", 100));

		bind(String.class).annotatedWith(Names.named("config:acks"))
				.toProvider(new ConfigValueProvider.Str("ACKS", "all"));

		bind(Properties.class).annotatedWith(Names.named("producer")).toProvider(ProducerPropsProvider.class);
		bind(Properties.class).annotatedWith(Names.named("consumer")).toProvider(ConsumerPropsProvider.class);
	}

}
