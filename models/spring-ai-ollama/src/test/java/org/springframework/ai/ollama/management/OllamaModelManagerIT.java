package org.springframework.ai.ollama.management;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.ai.ollama.BaseOllamaIT;
import org.springframework.ai.ollama.api.OllamaModel;

import static org.assertj.core.api.Assertions.assertThat;

class OllamaModelManagerIT extends BaseOllamaIT {

	private static final String MODEL = OllamaModel.NOMIC_EMBED_TEXT.getName();

	static OllamaModelManager modelManager;

	@BeforeAll
	public static void beforeAll() throws IOException, InterruptedException {
		var ollamaApi = initializeOllama(MODEL);
		modelManager = new OllamaModelManager(ollamaApi);
	}

	@Test
	public void whenModelAvailableReturnTrue() {
		var isModelAvailable = modelManager.isModelAvailable(MODEL);
		assertThat(isModelAvailable).isTrue();

		isModelAvailable = modelManager.isModelAvailable(MODEL + ":latest");
		assertThat(isModelAvailable).isTrue();
	}

	@Test
	public void whenModelNotAvailableReturnFalse() {
		var isModelAvailable = modelManager.isModelAvailable("aleph");
		assertThat(isModelAvailable).isFalse();
	}

	@Test
	@Disabled("This test is brittle and fails often in CI")
	public void pullAndDeleteModelFromOllama() {

		var modelWithExplicitVersion = "all-minilm:33m";
		modelManager.deleteModel(modelWithExplicitVersion);
		modelManager.pullModel(modelWithExplicitVersion, PullModelStrategy.WHEN_MISSING);
		var isModelWithExplicitVersionAvailable = modelManager.isModelAvailable(modelWithExplicitVersion);
		assertThat(isModelWithExplicitVersionAvailable).isTrue();

		var modelWithoutVersion = "all-minilm";
		modelManager.deleteModel(modelWithoutVersion);
		var isModelWithoutVersionAvailable = modelManager.isModelAvailable(modelWithoutVersion);
		assertThat(isModelWithoutVersionAvailable).isFalse();
		isModelWithExplicitVersionAvailable = modelManager.isModelAvailable(modelWithExplicitVersion);
		assertThat(isModelWithExplicitVersionAvailable).isTrue();

		modelManager.pullModel(modelWithoutVersion, PullModelStrategy.WHEN_MISSING);
		isModelWithoutVersionAvailable = modelManager.isModelAvailable(modelWithoutVersion);
		assertThat(isModelWithoutVersionAvailable).isTrue();

		var modelWithLatestVersion = "all-minilm:latest";
		var isModelWithLatestVersionAvailable = modelManager.isModelAvailable(modelWithLatestVersion);
		assertThat(isModelWithLatestVersionAvailable).isTrue();

		modelManager.deleteModel(modelWithExplicitVersion);
		isModelWithExplicitVersionAvailable = modelManager.isModelAvailable(modelWithExplicitVersion);
		assertThat(isModelWithExplicitVersionAvailable).isFalse();

		modelManager.deleteModel(modelWithLatestVersion);
		isModelWithLatestVersionAvailable = modelManager.isModelAvailable(modelWithLatestVersion);
		assertThat(isModelWithLatestVersionAvailable).isFalse();
	}

	@Disabled
	@Test
	public void pullAndDeleteModelFromHuggingFace() {

		var modelWithExplicitVersion = "hf.co/SanctumAI/Llama-3.2-1B-Instruct-GGUF:Q3_K_S";
		modelManager.deleteModel(modelWithExplicitVersion);
		modelManager.pullModel(modelWithExplicitVersion, PullModelStrategy.WHEN_MISSING);
		var isModelWithExplicitVersionAvailable = modelManager.isModelAvailable(modelWithExplicitVersion);
		assertThat(isModelWithExplicitVersionAvailable).isTrue();

		var modelWithoutVersion = "hf.co/SanctumAI/Llama-3.2-1B-Instruct-GGUF";
		modelManager.deleteModel(modelWithoutVersion);
		var isModelWithoutVersionAvailable = modelManager.isModelAvailable(modelWithoutVersion);
		assertThat(isModelWithoutVersionAvailable).isFalse();
		isModelWithExplicitVersionAvailable = modelManager.isModelAvailable(modelWithExplicitVersion);
		assertThat(isModelWithExplicitVersionAvailable).isTrue();

		modelManager.pullModel(modelWithoutVersion, PullModelStrategy.WHEN_MISSING);
		isModelWithoutVersionAvailable = modelManager.isModelAvailable(modelWithoutVersion);
		assertThat(isModelWithoutVersionAvailable).isTrue();

		var modelWithLatestVersion = "hf.co/SanctumAI/Llama-3.2-1B-Instruct-GGUF:latest";
		var isModelWithLatestVersionAvailable = modelManager.isModelAvailable(modelWithLatestVersion);
		assertThat(isModelWithLatestVersionAvailable).isTrue();

		modelManager.deleteModel(modelWithExplicitVersion);
		isModelWithExplicitVersionAvailable = modelManager.isModelAvailable(modelWithExplicitVersion);
		assertThat(isModelWithExplicitVersionAvailable).isFalse();

		modelManager.deleteModel(modelWithLatestVersion);
		isModelWithLatestVersionAvailable = modelManager.isModelAvailable(modelWithLatestVersion);
		assertThat(isModelWithLatestVersionAvailable).isFalse();
	}

	@Test
	@Disabled("This test is brittle and fails often in CI")
	public void pullAdditionalModels() {
		var model = "all-minilm";
		var isModelAvailable = modelManager.isModelAvailable(model);
		assertThat(isModelAvailable).isFalse();

		new OllamaModelManager(getOllamaApi(),
				new ModelManagementOptions(PullModelStrategy.WHEN_MISSING, List.of(model), Duration.ofMinutes(5), 0));

		isModelAvailable = modelManager.isModelAvailable(model);
		assertThat(isModelAvailable).isTrue();

		modelManager.deleteModel(model);
		isModelAvailable = modelManager.isModelAvailable(model);
		assertThat(isModelAvailable).isFalse();
	}

}
