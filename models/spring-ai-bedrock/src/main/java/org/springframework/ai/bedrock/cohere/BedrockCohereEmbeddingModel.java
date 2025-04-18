package org.springframework.ai.bedrock.cohere;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingRequest;
import org.springframework.ai.bedrock.cohere.api.CohereEmbeddingBedrockApi.CohereEmbeddingResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.util.Assert;

public class BedrockCohereEmbeddingModel extends AbstractEmbeddingModel {

	private static final int COHERE_MAX_CHARACTERS = 2048;

	private final CohereEmbeddingBedrockApi embeddingApi;

	private final BedrockCohereEmbeddingOptions defaultOptions;

	public BedrockCohereEmbeddingModel(CohereEmbeddingBedrockApi cohereEmbeddingBedrockApi) {
		this(cohereEmbeddingBedrockApi,
				BedrockCohereEmbeddingOptions.builder()
					.inputType(CohereEmbeddingRequest.InputType.SEARCH_DOCUMENT)
					.truncate(CohereEmbeddingRequest.Truncate.NONE)
					.build());
	}

	public BedrockCohereEmbeddingModel(CohereEmbeddingBedrockApi cohereEmbeddingBedrockApi,
			BedrockCohereEmbeddingOptions options) {
		Assert.notNull(cohereEmbeddingBedrockApi, "CohereEmbeddingBedrockApi must not be null");
		Assert.notNull(options, "BedrockCohereEmbeddingOptions must not be null");
		this.embeddingApi = cohereEmbeddingBedrockApi;
		this.defaultOptions = options;
	}

	@Override
	public float[] embed(Document document) {
		return embed(document.getText());
	}

	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {

		List<String> instructions = request.getInstructions();
		Assert.notEmpty(instructions, "At least one text is required!");

		final BedrockCohereEmbeddingOptions optionsToUse = this.mergeOptions(request.getOptions());

		List<String> truncatedInstructions = instructions.stream().map(text -> {
			if (text == null || text.isEmpty()) {
				return text;
			}

			if (text.length() <= COHERE_MAX_CHARACTERS) {
				return text;
			}

			return switch (optionsToUse.getTruncate()) {
				case END -> text.substring(0, COHERE_MAX_CHARACTERS);

				case START -> text.substring(text.length() - COHERE_MAX_CHARACTERS);

				default -> text.substring(0, COHERE_MAX_CHARACTERS);

			};
		}).collect(Collectors.toList());

		var apiRequest = new CohereEmbeddingRequest(truncatedInstructions, optionsToUse.getInputType(),
				optionsToUse.getTruncate());
		CohereEmbeddingResponse apiResponse = this.embeddingApi.embedding(apiRequest);
		var indexCounter = new AtomicInteger(0);
		List<Embedding> embeddings = apiResponse.embeddings()
			.stream()
			.map(e -> new Embedding(e, indexCounter.getAndIncrement()))
			.toList();
		return new EmbeddingResponse(embeddings);
	}

	BedrockCohereEmbeddingOptions mergeOptions(EmbeddingOptions requestOptions) {

		BedrockCohereEmbeddingOptions options = (this.defaultOptions != null) ? this.defaultOptions
				: BedrockCohereEmbeddingOptions.builder()
					.inputType(CohereEmbeddingRequest.InputType.SEARCH_DOCUMENT)
					.truncate(CohereEmbeddingRequest.Truncate.NONE)
					.build();

		if (requestOptions != null) {
			options = ModelOptionsUtils.merge(requestOptions, options, BedrockCohereEmbeddingOptions.class);
		}

		return options;
	}

}
