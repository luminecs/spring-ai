package org.springframework.ai.ollama.api;

import java.io.IOException;
import java.time.Duration;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.ai.ollama.BaseOllamaIT;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

public class OllamaApiModelsIT extends BaseOllamaIT {

	private static final String MODEL = "all-minilm";

	static OllamaApi ollamaApi;

	@BeforeAll
	public static void beforeAll() throws IOException, InterruptedException {
		ollamaApi = initializeOllama(MODEL);
	}

	@Test
	public void listModels() {
		var listModelResponse = ollamaApi.listModels();

		assertThat(listModelResponse).isNotNull();
		assertThat(listModelResponse.models().size()).isGreaterThan(0);
		assertThat(listModelResponse.models().stream().anyMatch(model -> model.name().contains(MODEL))).isTrue();
	}

	@Test
	public void showModel() {
		var showModelRequest = new OllamaApi.ShowModelRequest(MODEL);
		var showModelResponse = ollamaApi.showModel(showModelRequest);

		assertThat(showModelResponse).isNotNull();
		assertThat(showModelResponse.details().family()).isEqualTo("bert");
	}

	@Test
	public void copyAndDeleteModel() {
		var customModel = "schrodinger";
		var copyModelRequest = new OllamaApi.CopyModelRequest(MODEL, customModel);
		var copyModelResponse = ollamaApi.copyModel(copyModelRequest);
		assertThat(copyModelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		var deleteModelRequest = new OllamaApi.DeleteModelRequest(customModel);
		var deleteModelResponse = ollamaApi.deleteModel(deleteModelRequest);
		assertThat(deleteModelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void pullModel() {
		var deleteModelRequest = new OllamaApi.DeleteModelRequest(MODEL);
		var deleteModelResponse = ollamaApi.deleteModel(deleteModelRequest);
		assertThat(deleteModelResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		var listModelResponse = ollamaApi.listModels();
		assertThat(listModelResponse.models().stream().anyMatch(model -> model.name().contains(MODEL))).isFalse();

		var pullModelRequest = new OllamaApi.PullModelRequest(MODEL);
		var progressResponses = ollamaApi.pullModel(pullModelRequest)
			.timeout(Duration.ofMinutes(5))
			.collectList()
			.block();

		assertThat(progressResponses).isNotNull();
		Awaitility.await().until(() -> {
			OllamaApi.ProgressResponse progressResponse = progressResponses.get(progressResponses.size() - 1);
			return progressResponse.status().equals("success");
		});
		assertThat(progressResponses.get(progressResponses.size() - 1))
			.isEqualTo(new OllamaApi.ProgressResponse("success", null, null, null));

		listModelResponse = ollamaApi.listModels();
		assertThat(listModelResponse.models().stream().anyMatch(model -> model.name().contains(MODEL))).isTrue();
	}

}
