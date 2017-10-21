package se.yolean.kafka.test.failover.config;

import java.util.Properties;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import se.yolean.kafka.test.failover.analytics.TestMessage;

public class ConfigModule extends AbstractModule {

	/**
	 * Depends on ID logic in {@link TestMessage}
	 */
	private static final int DEFAULT_MESSAGES_MAX = 999999999;

	public static final String DEFAULT_BOOTSTRAP_SERVERS = "192.168.99.100:32400,192.168.99.100:32401,192.168.99.100:32402";
	public static final String DEFAULT_TOPIC = "test-produce-consume-latency";

	private ILogger log = SLoggerFactory.getLogger(this.getClass());

	String getEnv(String envName) {
		return System.getenv(envName);
	}

	public String getConf(String envName, String fallback) {
		String conf = getEnv(envName);
		if (conf != null) {
			log.info("conf", "name", envName, "value", conf);
			return conf;
		}
		log.info("conf", "name", envName, "default", fallback);
		return fallback;
	}

	public int getConf(String envName, int fallback) {
		String conf = getEnv(envName);
		if (conf != null) {
			log.info("conf", "name", envName, "value", conf);
			return Integer.parseInt(conf);
		}
		log.info("conf", "name", envName, "default", fallback);
		return fallback;
	}

	@Override
	protected void configure() {
		String bs = getConf("BOOTSTRAP", DEFAULT_BOOTSTRAP_SERVERS);
		bind(String.class).annotatedWith(Names.named("config:bootstrap")).toInstance(bs);

		String topics = getConf("TOPIC", DEFAULT_TOPIC);
		bind(String.class).annotatedWith(Names.named("config:topic")).toInstance(topics);

		bind(Integer.class).annotatedWith(Names.named("config:messagesMax"))
				.toInstance(getConf("MESSAGES_MAX", DEFAULT_MESSAGES_MAX));
		bind(Integer.class).annotatedWith(Names.named("config:messageIntervalMs"))
				.toInstance(getConf("MESSAGE_INTERVAL", 1000));
		bind(Integer.class).annotatedWith(Names.named("config:ackTimeoutMs"))
				.toInstance(getConf("ACK_TIMEOUT_MS", 100));

		bind(String.class).annotatedWith(Names.named("config:acks")).toInstance(getConf("ACKS", "all"));

		bind(Properties.class).annotatedWith(Names.named("producerDefaults"))
				.toProvider(ProducerDefaultPropsProvider.class);
		bind(Properties.class).annotatedWith(Names.named("consumerDefaults"))
				.toProvider(ConsumerDefaultPropsProvider.class);
	}

}
