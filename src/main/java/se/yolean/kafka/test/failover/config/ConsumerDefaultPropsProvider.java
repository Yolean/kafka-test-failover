package se.yolean.kafka.test.failover.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class ConsumerDefaultPropsProvider implements Provider<Properties> {

	private String bootstrap;

	@Inject
	public ConsumerDefaultPropsProvider(@Named("config:bootstrap") String bootstrap) {
		this.bootstrap = bootstrap;
	}	
	
	@Override
	public Properties get() {
		// https://kafka.apache.org/0110/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html
		// Automatic Offset Committing
	     Properties props = new Properties();
	     props.put("bootstrap.servers", bootstrap);
	     props.put("group.id", "test");
	     props.put("enable.auto.commit", "true");
	     props.put("auto.commit.interval.ms", "1000");
	     props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	     props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		return props;
	}

}
