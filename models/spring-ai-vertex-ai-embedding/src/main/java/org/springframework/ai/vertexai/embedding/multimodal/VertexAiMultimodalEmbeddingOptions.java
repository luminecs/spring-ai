package org.springframework.ai.vertexai.embedding.multimodal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.util.StringUtils;

@JsonInclude(Include.NON_NULL)
public class VertexAiMultimodalEmbeddingOptions implements EmbeddingOptions {

	public static final String DEFAULT_MODEL_NAME = VertexAiMultimodalEmbeddingModelName.MULTIMODAL_EMBEDDING_001
		.getName();

	// @formatter:off

	private @JsonProperty("model") String model;

	private @JsonProperty("dimensions") Integer dimensions;

	private @JsonProperty("videoStartOffsetSec") Integer videoStartOffsetSec;

	private @JsonProperty("videoEndOffsetSec") Integer videoEndOffsetSec;

	private @JsonProperty("videoIntervalSec") Integer videoIntervalSec;

	// @formatter:on
	public static Builder builder() {
		return new Builder();
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public Integer getDimensions() {
		return this.dimensions;
	}

	public void setDimensions(Integer dimensions) {
		this.dimensions = dimensions;
	}

	public Integer getVideoStartOffsetSec() {
		return this.videoStartOffsetSec;
	}

	public void setVideoStartOffsetSec(Integer videoStartOffsetSec) {
		this.videoStartOffsetSec = videoStartOffsetSec;
	}

	public Integer getVideoEndOffsetSec() {
		return this.videoEndOffsetSec;
	}

	public void setVideoEndOffsetSec(Integer videoEndOffsetSec) {
		this.videoEndOffsetSec = videoEndOffsetSec;
	}

	public Integer getVideoIntervalSec() {
		return this.videoIntervalSec;
	}

	public void setVideoIntervalSec(Integer videoIntervalSec) {
		this.videoIntervalSec = videoIntervalSec;
	}

	public static class Builder {

		protected VertexAiMultimodalEmbeddingOptions options;

		public Builder() {
			this.options = new VertexAiMultimodalEmbeddingOptions();
		}

		public Builder from(VertexAiMultimodalEmbeddingOptions fromOptions) {
			if (fromOptions.getDimensions() != null) {
				this.options.setDimensions(fromOptions.getDimensions());
			}
			if (StringUtils.hasText(fromOptions.getModel())) {
				this.options.setModel(fromOptions.getModel());
			}
			if (fromOptions.getVideoStartOffsetSec() != null) {
				this.options.setVideoStartOffsetSec(fromOptions.getVideoStartOffsetSec());
			}
			if (fromOptions.getVideoEndOffsetSec() != null) {
				this.options.setVideoEndOffsetSec(fromOptions.getVideoEndOffsetSec());
			}
			if (fromOptions.getVideoIntervalSec() != null) {
				this.options.setVideoIntervalSec(fromOptions.getVideoIntervalSec());
			}
			return this;
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder model(VertexAiMultimodalEmbeddingModelName model) {
			this.options.setModel(model.getName());
			return this;
		}

		public Builder dimensions(Integer dimensions) {
			this.options.setDimensions(dimensions);
			return this;
		}

		public Builder videoStartOffsetSec(Integer videoStartOffsetSec) {
			this.options.setVideoStartOffsetSec(videoStartOffsetSec);
			return this;
		}

		public Builder videoEndOffsetSec(Integer videoEndOffsetSec) {
			this.options.setVideoEndOffsetSec(videoEndOffsetSec);
			return this;
		}

		public Builder videoIntervalSec(Integer videoIntervalSec) {
			this.options.setVideoIntervalSec(videoIntervalSec);
			return this;
		}

		public VertexAiMultimodalEmbeddingOptions build() {
			return this.options;
		}

	}

}
