package org.springframework.ai.watsonx;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.watsonx.api.WatsonxAiApi;
import org.springframework.ai.watsonx.api.WatsonxAiEmbeddingRequest;
import org.springframework.ai.watsonx.api.WatsonxAiEmbeddingResponse;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class WatsonxAiEmbeddingModel extends AbstractEmbeddingModel {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final WatsonxAiApi watsonxAiApi;

	private WatsonxAiEmbeddingOptions defaultOptions = WatsonxAiEmbeddingOptions.create()
		.withModel(WatsonxAiEmbeddingOptions.DEFAULT_MODEL);

	public WatsonxAiEmbeddingModel(WatsonxAiApi watsonxAiApi) {
		this.watsonxAiApi = watsonxAiApi;
	}

	public WatsonxAiEmbeddingModel(WatsonxAiApi watsonxAiApi, WatsonxAiEmbeddingOptions defaultOptions) {
		this.watsonxAiApi = watsonxAiApi;
		this.defaultOptions = defaultOptions;
	}

	@Override
	public float[] embed(Document document) {
		return embed(document.getText());
	}

	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {
		Assert.notEmpty(request.getInstructions(), "At least one text is required!");

		WatsonxAiEmbeddingRequest embeddingRequest = watsonxAiEmbeddingRequest(request.getInstructions(),
				request.getOptions());
		WatsonxAiEmbeddingResponse response = this.watsonxAiApi.embeddings(embeddingRequest).getBody();

		AtomicInteger indexCounter = new AtomicInteger(0);
		List<Embedding> embeddings = response.results()
			.stream()
			.map(e -> new Embedding(e.embedding(), indexCounter.getAndIncrement()))
			.toList();

		return new EmbeddingResponse(embeddings);
	}

	WatsonxAiEmbeddingRequest watsonxAiEmbeddingRequest(List<String> inputs, EmbeddingOptions options) {

		WatsonxAiEmbeddingOptions runtimeOptions = (options instanceof WatsonxAiEmbeddingOptions)
				? (WatsonxAiEmbeddingOptions) options : this.defaultOptions;

		if (!StringUtils.hasText(runtimeOptions.getModel())) {
			logger.warn("The model cannot be null, using default model instead");
			runtimeOptions = this.defaultOptions;
		}

		return WatsonxAiEmbeddingRequest.builder(inputs).withModel(runtimeOptions.getModel()).build();
	}

}
