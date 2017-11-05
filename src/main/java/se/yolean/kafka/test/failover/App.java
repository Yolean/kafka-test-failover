package se.yolean.kafka.test.failover;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.prometheus.client.exporter.HTTPServer;
import se.yolean.kafka.test.failover.config.AppModule;
import se.yolean.kafka.test.failover.config.ConfigModule;
import se.yolean.kafka.test.failover.config.MetricsModule;

public class App {

	public static final int DEFAULT_PROMETHEUS_EXPORTER_PORT = 5000;

	public static final int DEFAULT_RUNS = 1;

	static {
		com.github.structlog4j.StructLog4J.setFormatter(com.github.structlog4j.json.JsonFormatter.getInstance());
	}

	private static ILogger log = SLoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		ConfigModule configModule = new ConfigModule();
		int serverPort = configModule.getConf("PORT", DEFAULT_PROMETHEUS_EXPORTER_PORT);
		Injector injector = Guice.createInjector(configModule, new AppModule(), new MetricsModule(serverPort));

		HTTPServer server = injector.getInstance(HTTPServer.class);
		if (server == null)
			throw new IllegalStateException("No prometheus export server found");
		log.info("Prometheus export server running", "port", serverPort, "instance", server);

		String appId = configModule.getConf("KEY_PREFIX", "KT");

		int runs = DEFAULT_RUNS;

		for (int i = 0; i < runs; i++) {
			RunId runId = new RunId(appId);
			ProducerConsumerRun run = injector.getInstance(ProducerConsumerRun.class);
			log.info("New run", "appId", appId, "runId", runId);
			run.setRunId(runId);
			Thread t = new Thread(run);
			t.start();
		}

		Thread shutdown = new Thread(new Runnable() {
			@Override
			public void run() {
				server.stop();
			}
		});
		Runtime.getRuntime().addShutdownHook(shutdown);
	}

}
