package org.springframework.ai.postgresml;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.postgresml.PostgresMlEmbeddingModel.VectorType;

@JsonInclude(Include.NON_NULL)
public class PostgresMlEmbeddingOptions implements EmbeddingOptions {

	// @formatter:off

	private @JsonProperty("transformer") String transformer = PostgresMlEmbeddingModel.DEFAULT_TRANSFORMER_MODEL;

	private @JsonProperty("vectorType") VectorType vectorType = VectorType.PG_ARRAY;

	private @JsonProperty("kwargs") Map<String, Object> kwargs = Map.of();

	private @JsonProperty("metadataMode") MetadataMode metadataMode = MetadataMode.EMBED;
	// @formatter:on

	public static Builder builder() {
		return new Builder();
	}

	public String getTransformer() {
		return this.transformer;
	}

	public void setTransformer(String transformer) {
		this.transformer = transformer;
	}

	public VectorType getVectorType() {
		return this.vectorType;
	}

	public void setVectorType(VectorType vectorType) {
		this.vectorType = vectorType;
	}

	public Map<String, Object> getKwargs() {
		return this.kwargs;
	}

	public void setKwargs(Map<String, Object> kwargs) {
		this.kwargs = kwargs;
	}

	public MetadataMode getMetadataMode() {
		return this.metadataMode;
	}

	public void setMetadataMode(MetadataMode metadataMode) {
		this.metadataMode = metadataMode;
	}

	@Override
	@JsonIgnore
	public String getModel() {
		return null;
	}

	@Override
	@JsonIgnore
	public Integer getDimensions() {
		return null;
	}

	public static class Builder {

		protected PostgresMlEmbeddingOptions options;

		public Builder() {
			this.options = new PostgresMlEmbeddingOptions();
		}

		public Builder transformer(String transformer) {
			this.options.setTransformer(transformer);
			return this;
		}

		public Builder vectorType(VectorType vectorType) {
			this.options.setVectorType(vectorType);
			return this;
		}

		public Builder kwargs(String kwargs) {
			this.options.setKwargs(ModelOptionsUtils.objectToMap(kwargs));
			return this;
		}

		public Builder kwargs(Map<String, Object> kwargs) {
			this.options.setKwargs(kwargs);
			return this;
		}

		public Builder metadataMode(MetadataMode metadataMode) {
			this.options.setMetadataMode(metadataMode);
			return this;
		}

		public PostgresMlEmbeddingOptions build() {
			return this.options;
		}

	}

}
