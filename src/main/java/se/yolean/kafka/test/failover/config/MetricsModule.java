package se.yolean.kafka.test.failover.config;

import java.io.IOException;

import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import io.prometheus.client.exporter.HTTPServer;

public class MetricsModule extends AbstractModule {

	public static final int DEFAULT_PROMETHEUS_EXPORTER_PORT = 5000;

	public final Provider<Integer> port = new ConfigValueProvider.Int("PORT", DEFAULT_PROMETHEUS_EXPORTER_PORT);

	@Override
	protected void configure() {
		bind(Integer.class).annotatedWith(Names.named("config:prometheusExporterPort")).toProvider(port);
		HTTPServer server;
		try {
			server = new HTTPServer(port.get());
		} catch (IOException e) {
			throw new RuntimeException("Failed to start metrics exporter on port " + port.get(), e);
		}
		bind(HTTPServer.class).toInstance(server);
	}

}
