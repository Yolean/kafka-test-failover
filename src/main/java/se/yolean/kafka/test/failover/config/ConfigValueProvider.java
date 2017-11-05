package se.yolean.kafka.test.failover.config;

import java.util.regex.Pattern;

import javax.inject.Provider;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

abstract class ConfigValueProvider<T> implements Provider<T> {

	protected ILogger log = SLoggerFactory.getLogger(this.getClass());
	
	public static Pattern PARAM_NAME_RULE = Pattern.compile("^[A-Z][A-Z_]+[A-Z]$");
	
	private T fallback;
	private String paramName;
	private T cachedValue;

	/**
	 * @param paramName Typically the name of an environment variable
	 * @param fallback Default value
	 */
	protected ConfigValueProvider(String paramName, T fallback) {
		if (!PARAM_NAME_RULE.matcher(paramName).matches()) {
			throw new IllegalArgumentException("Invalid config param name " + paramName);
		}
		this.paramName = paramName;
		this.fallback = fallback;
		this.cachedValue = readEnv();
	}
	
	protected final T getFallback() {
		return fallback;
	}
	
	protected final String getParamName() {
		return paramName;
	}
	
	@Override
	public T get() {
		return cachedValue;
	};
	
	protected abstract T parse(String value);
	
	private T readEnv() {
		T v = getFallback();
		String read = System.getenv(getParamName());
		if (read != null) {
			v = parse(read);
			log.info("conf", "name", getParamName(), "value", v);
		} else {
			log.info("conf", "name", getParamName(), "default", v);
		}
		return v;
	}
	
	static class Str extends ConfigValueProvider<String> {

		protected Str(String paramName, String fallback) {
			super(paramName, fallback);
		}

		@Override
		protected String parse(String value) {
			return value;
		}
		
	}
	
	static class Int extends ConfigValueProvider<Integer> {

		protected Int(String paramName, Integer fallback) {
			super(paramName, fallback);
		}

		@Override
		protected Integer parse(String value) {
			return Integer.parseInt(value);
		}
		
	}

}
