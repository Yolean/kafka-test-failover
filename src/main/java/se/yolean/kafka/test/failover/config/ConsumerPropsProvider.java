package se.yolean.kafka.test.failover.config;

import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import se.yolean.kafka.test.failover.RunId;

public class ConsumerPropsProvider implements Provider<Properties> {

	private String bootstrap;
	private RunId runId;

	@Inject
	public ConsumerPropsProvider(@Named("config:bootstrap") String bootstrap) {
		this.bootstrap = bootstrap;
	}
	
	@Inject
	public void setRunId(RunId runId) {
		this.runId = runId;
	}
	
	/**
	 * @return Something that is unique per run, because we didn't design for the consumer group feature.
	 */
	private String getConsumerGroupId() {
		return "kafka-test-failover-" + runId.toString();
	}
	
	@Override
	public Properties get() {
		// https://kafka.apache.org/0110/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html
		// Automatic Offset Committing
	     Properties props = new Properties();
	     props.put("bootstrap.servers", bootstrap);
	     props.put("group.id", getConsumerGroupId());
	     props.put("enable.auto.commit", "true");
	     props.put("auto.commit.interval.ms", "1000");
	     props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
	     props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		return props;
	}

}
