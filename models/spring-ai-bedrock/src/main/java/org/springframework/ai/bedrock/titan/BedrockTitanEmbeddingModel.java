package org.springframework.ai.bedrock.titan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingRequest;
import org.springframework.ai.bedrock.titan.api.TitanEmbeddingBedrockApi.TitanEmbeddingResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.util.Assert;

public class BedrockTitanEmbeddingModel extends AbstractEmbeddingModel {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final TitanEmbeddingBedrockApi embeddingApi;

	private InputType inputType = InputType.TEXT;

	public BedrockTitanEmbeddingModel(TitanEmbeddingBedrockApi titanEmbeddingBedrockApi) {
		this.embeddingApi = titanEmbeddingBedrockApi;
	}

	public BedrockTitanEmbeddingModel withInputType(InputType inputType) {
		this.inputType = inputType;
		return this;
	}

	@Override
	public float[] embed(Document document) {
		return embed(document.getText());
	}

	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {
		Assert.notEmpty(request.getInstructions(), "At least one text is required!");
		if (request.getInstructions().size() != 1) {
			logger.warn(
					"Titan Embedding does not support batch embedding. Will make multiple API calls to embed(Document)");
		}

		List<Embedding> embeddings = new ArrayList<>();
		var indexCounter = new AtomicInteger(0);
		for (String inputContent : request.getInstructions()) {
			var apiRequest = createTitanEmbeddingRequest(inputContent, request.getOptions());
			TitanEmbeddingResponse response = this.embeddingApi.embedding(apiRequest);
			embeddings.add(new Embedding(response.embedding(), indexCounter.getAndIncrement()));
		}
		return new EmbeddingResponse(embeddings);
	}

	private TitanEmbeddingRequest createTitanEmbeddingRequest(String inputContent, EmbeddingOptions requestOptions) {
		InputType inputType = this.inputType;

		if (requestOptions != null
				&& requestOptions instanceof BedrockTitanEmbeddingOptions bedrockTitanEmbeddingOptions) {
			inputType = bedrockTitanEmbeddingOptions.getInputType();
		}

		return (inputType == InputType.IMAGE) ? new TitanEmbeddingRequest.Builder().inputImage(inputContent).build()
				: new TitanEmbeddingRequest.Builder().inputText(inputContent).build();
	}

	@Override
	public int dimensions() {
		if (this.inputType == InputType.IMAGE) {
			if (this.embeddingDimensions.get() < 0) {
				this.embeddingDimensions.set(dimensions(this, this.embeddingApi.getModelId(),

						"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII="));
			}
		}
		return super.dimensions();

	}

	public enum InputType {

		TEXT, IMAGE

	}

}
