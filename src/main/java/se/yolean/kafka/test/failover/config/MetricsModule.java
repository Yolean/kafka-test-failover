package se.yolean.kafka.test.failover.config;

import java.io.IOException;

import com.google.inject.AbstractModule;

import io.prometheus.client.exporter.HTTPServer;

public class MetricsModule extends AbstractModule {

	public static final int PROMETHEUS_EXPORTER_PORT = 5000;
	
	@Override
	protected void configure() {
		int exporterPort = PROMETHEUS_EXPORTER_PORT;
		try {
			bind(HTTPServer.class).toInstance(new HTTPServer(exporterPort));
		} catch (IOException e) {
			throw new RuntimeException("Failed to start metrics exporter on port " + exporterPort, e);
		}
	}

}
