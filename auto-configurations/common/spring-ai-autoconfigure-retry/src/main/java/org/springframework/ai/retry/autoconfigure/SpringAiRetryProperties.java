package org.springframework.ai.retry.autoconfigure;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(SpringAiRetryProperties.CONFIG_PREFIX)
public class SpringAiRetryProperties {

	public static final String CONFIG_PREFIX = "spring.ai.retry";

	private int maxAttempts = 10;

	@NestedConfigurationProperty
	private Backoff backoff = new Backoff();

	private boolean onClientErrors = false;

	private List<Integer> excludeOnHttpCodes = new ArrayList<>();

	private List<Integer> onHttpCodes = new ArrayList<>();

	public int getMaxAttempts() {
		return this.maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public Backoff getBackoff() {
		return this.backoff;
	}

	public List<Integer> getExcludeOnHttpCodes() {
		return this.excludeOnHttpCodes;
	}

	public void setExcludeOnHttpCodes(List<Integer> onHttpCodes) {
		this.excludeOnHttpCodes = onHttpCodes;
	}

	public boolean isOnClientErrors() {
		return this.onClientErrors;
	}

	public void setOnClientErrors(boolean onClientErrors) {
		this.onClientErrors = onClientErrors;
	}

	public List<Integer> getOnHttpCodes() {
		return this.onHttpCodes;
	}

	public void setOnHttpCodes(List<Integer> onHttpCodes) {
		this.onHttpCodes = onHttpCodes;
	}

	public static class Backoff {

		private Duration initialInterval = Duration.ofMillis(2000);

		private int multiplier = 5;

		private Duration maxInterval = Duration.ofMillis(3 * 60000);

		public Duration getInitialInterval() {
			return this.initialInterval;
		}

		public void setInitialInterval(Duration initialInterval) {
			this.initialInterval = initialInterval;
		}

		public int getMultiplier() {
			return this.multiplier;
		}

		public void setMultiplier(int multiplier) {
			this.multiplier = multiplier;
		}

		public Duration getMaxInterval() {
			return this.maxInterval;
		}

		public void setMaxInterval(Duration maxInterval) {
			this.maxInterval = maxInterval;
		}

	}

}
