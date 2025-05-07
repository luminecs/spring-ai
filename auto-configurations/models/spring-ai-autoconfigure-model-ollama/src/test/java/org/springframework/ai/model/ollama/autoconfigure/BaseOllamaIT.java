package org.springframework.ai.model.ollama.autoconfigure;

import java.time.Duration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.ollama.OllamaContainer;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.OllamaModelManager;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.util.Assert;

@Testcontainers
@EnabledIfEnvironmentVariable(named = "OLLAMA_AUTOCONF_TESTS_ENABLED", matches = "true")
public abstract class BaseOllamaIT {

	static {
		System.out.println("OLLAMA_AUTOCONF_TESTS_ENABLED=" + System.getenv("OLLAMA_AUTOCONF_TESTS_ENABLED"));
		System.out.println("System property=" + System.getProperty("OLLAMA_AUTOCONF_TESTS_ENABLED"));
	}
	private static final String OLLAMA_LOCAL_URL = "http://localhost:11434";

	private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(10);

	private static final int DEFAULT_MAX_RETRIES = 2;

	private static final boolean SKIP_CONTAINER_CREATION = Boolean
		.parseBoolean(System.getenv().getOrDefault("OLLAMA_WITH_REUSE", "false"));

	private static OllamaContainer ollamaContainer;

	private static final ThreadLocal<OllamaApi> ollamaApi = new ThreadLocal<>();

	protected static OllamaApi initializeOllama(final String model) {
		Assert.hasText(model, "Model name must be provided");

		if (!SKIP_CONTAINER_CREATION) {
			ollamaContainer = new OllamaContainer(OllamaImage.DEFAULT_IMAGE).withReuse(true);
			ollamaContainer.start();
		}

		final OllamaApi api = buildOllamaApiWithModel(model);
		ollamaApi.set(api);
		return api;
	}

	protected static OllamaApi getOllamaApi() {
		OllamaApi api = ollamaApi.get();
		Assert.state(api != null, "OllamaApi not initialized. Call initializeOllama first.");
		return api;
	}

	@AfterAll
	public static void tearDown() {
		if (ollamaContainer != null) {
			ollamaContainer.stop();
		}
	}

	public static OllamaApi buildOllamaApiWithModel(final String model) {
		final String baseUrl = SKIP_CONTAINER_CREATION ? OLLAMA_LOCAL_URL : ollamaContainer.getEndpoint();
		final OllamaApi api = OllamaApi.builder().baseUrl(baseUrl).build();
		ensureModelIsPresent(api, model);
		return api;
	}

	public String getBaseUrl() {
		String baseUrl = SKIP_CONTAINER_CREATION ? OLLAMA_LOCAL_URL : ollamaContainer.getEndpoint();
		return baseUrl;
	}

	private static void ensureModelIsPresent(final OllamaApi ollamaApi, final String model) {
		final var modelManagementOptions = ModelManagementOptions.builder()
			.maxRetries(DEFAULT_MAX_RETRIES)
			.timeout(DEFAULT_TIMEOUT)
			.build();
		final var ollamaModelManager = new OllamaModelManager(ollamaApi, modelManagementOptions);
		ollamaModelManager.pullModel(model, PullModelStrategy.WHEN_MISSING);
	}

}
