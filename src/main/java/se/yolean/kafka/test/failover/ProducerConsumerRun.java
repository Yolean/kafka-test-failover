package se.yolean.kafka.test.failover;

import javax.inject.Inject;
import javax.inject.Named;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class ProducerConsumerRun {

	private ILogger log = SLoggerFactory.getLogger(this.getClass());
	
	@Inject @Named("config:bootstrap") private String bootstrap;
	
	@Inject @Named("config:topic") private String topic;
	
	public void start() {
		log.info("Starting", "bootstrap", bootstrap, "topic", topic);
	}
	
}
