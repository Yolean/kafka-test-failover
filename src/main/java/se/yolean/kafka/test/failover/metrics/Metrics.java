package se.yolean.kafka.test.failover.metrics;

import java.io.IOException;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;

import io.prometheus.client.exporter.HTTPServer;

public class Metrics {
	
	public static final int SERVER_PORT = 5000;

	final Counter iterations = Counter.build().name("iterations")
			.help("Test loop iterations run so far").register();

	public final Histogram iterationLatency = Histogram.build().name("iterationLatency")
			.help("Time taken for each test loop, excluding initial wait").register();

	final Gauge unseenSentMessages = Gauge.build().name("unseen_sent_messages")
			.help("Messages created that haven't been seen consumed").register();

	final Gauge unseenAckdMessages = Gauge.build().name("unseen_ackd_messages")
			.help("Messaced produced+acked that haven't been seen consumed").register();	
	
	private ILogger log = SLoggerFactory.getLogger(this.getClass());
	
	private static Metrics instance = null;
	
	private HTTPServer server;
	
	public Metrics() {
		if (instance != null) {
			throw new RuntimeException("Metrics must run as singleton, as we expose only one client");
		}
		instance = this;
		start();
	}
	
	private void start() {
		try {
			server = new HTTPServer(SERVER_PORT);
		} catch (IOException e) {
			log.error("Failed to start prometheus exporter server on port " + SERVER_PORT, e);
		}
	}
	
	void stop() {
		server.stop();
	}
	
}
