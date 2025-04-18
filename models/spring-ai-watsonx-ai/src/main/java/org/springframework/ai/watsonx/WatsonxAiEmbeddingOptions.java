package org.springframework.ai.watsonx;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.embedding.EmbeddingOptions;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WatsonxAiEmbeddingOptions implements EmbeddingOptions {

	public static final String DEFAULT_MODEL = "ibm/slate-30m-english-rtrvr";

	@JsonProperty("model_id")
	private String model;

	public static WatsonxAiEmbeddingOptions create() {
		return new WatsonxAiEmbeddingOptions();
	}

	public static WatsonxAiEmbeddingOptions fromOptions(WatsonxAiEmbeddingOptions fromOptions) {
		return new WatsonxAiEmbeddingOptions().withModel(fromOptions.getModel());
	}

	public WatsonxAiEmbeddingOptions withModel(String model) {
		this.model = model;
		return this;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	@JsonIgnore
	public Integer getDimensions() {
		return null;
	}

}
