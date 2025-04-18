package org.springframework.ai.oci;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.oracle.bmc.generativeaiinference.model.EmbedTextDetails;

import org.springframework.ai.embedding.EmbeddingOptions;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OCIEmbeddingOptions implements EmbeddingOptions {

	private @JsonProperty("model") String model;

	private @JsonProperty("compartment") String compartment;

	private @JsonProperty("servingMode") String servingMode;

	private @JsonProperty("truncate") EmbedTextDetails.Truncate truncate;

	public static Builder builder() {
		return new Builder();
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public Integer getDimensions() {
		return null;
	}

	public String getCompartment() {
		return this.compartment;
	}

	public void setCompartment(String compartment) {
		this.compartment = compartment;
	}

	public String getServingMode() {
		return this.servingMode;
	}

	public void setServingMode(String servingMode) {
		this.servingMode = servingMode;
	}

	public EmbedTextDetails.Truncate getTruncate() {
		return this.truncate;
	}

	public void setTruncate(EmbedTextDetails.Truncate truncate) {
		this.truncate = truncate;
	}

	public static class Builder {

		private final OCIEmbeddingOptions options = new OCIEmbeddingOptions();

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder compartment(String compartment) {
			this.options.setCompartment(compartment);
			return this;
		}

		public Builder servingMode(String servingMode) {
			this.options.setServingMode(servingMode);
			return this;
		}

		public Builder truncate(EmbedTextDetails.Truncate truncate) {
			this.options.truncate = truncate;
			return this;
		}

		public OCIEmbeddingOptions build() {
			return this.options;
		}

	}

}
