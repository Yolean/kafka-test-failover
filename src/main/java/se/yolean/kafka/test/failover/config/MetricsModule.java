package se.yolean.kafka.test.failover.config;

import java.io.IOException;

import com.google.inject.AbstractModule;

import io.prometheus.client.exporter.HTTPServer;

public class MetricsModule extends AbstractModule {

	private int prometheusExporterPort;

	public MetricsModule(int prometheusExporterPort) {
		this.prometheusExporterPort = prometheusExporterPort;
	}

	@Override
	protected void configure() {
		int exporterPort = prometheusExporterPort;
		try {
			bind(HTTPServer.class).toInstance(new HTTPServer(exporterPort));
		} catch (IOException e) {
			throw new RuntimeException("Failed to start metrics exporter on port " + exporterPort, e);
		}
	}

}
