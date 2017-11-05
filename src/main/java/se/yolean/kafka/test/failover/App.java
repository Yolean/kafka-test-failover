package se.yolean.kafka.test.failover;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.prometheus.client.exporter.HTTPServer;
import se.yolean.kafka.test.failover.config.AppModule;
import se.yolean.kafka.test.failover.config.MetricsModule;
import se.yolean.kafka.test.failover.config.RunModule;

public class App {

	public static final int DEFAULT_RUNS = 1;

	static {
		com.github.structlog4j.StructLog4J.setFormatter(com.github.structlog4j.json.JsonFormatter.getInstance());
	}

	private static ILogger log = SLoggerFactory.getLogger(App.class);

	public static void main(String[] args) {
		AppModule appModule = new AppModule();
		MetricsModule metricsModule = new MetricsModule();
		Injector appInjector = Guice.createInjector(appModule, metricsModule);

		HTTPServer server = appInjector.getInstance(HTTPServer.class);
		if (server == null)
			throw new IllegalStateException("No prometheus export server found");
		log.info("Prometheus export server running", "port", metricsModule.port.get(), "instance", server);

		String appId = appModule.appId.get();

		int runs = DEFAULT_RUNS;

		for (int i = 0; i < runs; i++) {
			RunId runId = new RunId(appId);
			Injector runInjector = appInjector.createChildInjector(new RunModule(runId));
			ProducerConsumerRun run = runInjector.getInstance(ProducerConsumerRun.class);
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
