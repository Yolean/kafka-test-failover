package se.yolean.kafka.test.failover.config;

import javax.inject.Provider;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class AppModule extends AbstractModule {

	public final Provider<String> appId = new ConfigValueProvider.Str("KEY_PREFIX", "KT");

	@Override
	protected void configure() {
		bind(String.class).annotatedWith(Names.named("config:appId")).toProvider(appId);
	}

}
