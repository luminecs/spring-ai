package org.springframework.ai.model.oci.genai.autoconfigure;

import com.oracle.bmc.generativeaiinference.model.EmbedTextDetails;

import org.springframework.ai.oci.OCIEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(OCIEmbeddingModelProperties.CONFIG_PREFIX)
public class OCIEmbeddingModelProperties {

	public static final String CONFIG_PREFIX = "spring.ai.oci.genai.embedding";

	private ServingMode servingMode = ServingMode.ON_DEMAND;

	private EmbedTextDetails.Truncate truncate = EmbedTextDetails.Truncate.End;

	private String compartment;

	private String model;

	public OCIEmbeddingOptions getEmbeddingOptions() {
		return OCIEmbeddingOptions.builder()
			.compartment(this.compartment)
			.model(this.model)
			.servingMode(this.servingMode.getMode())
			.truncate(this.truncate)
			.build();
	}

	public ServingMode getServingMode() {
		return this.servingMode;
	}

	public void setServingMode(ServingMode servingMode) {
		this.servingMode = servingMode;
	}

	public String getCompartment() {
		return this.compartment;
	}

	public void setCompartment(String compartment) {
		this.compartment = compartment;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public EmbedTextDetails.Truncate getTruncate() {
		return this.truncate;
	}

	public void setTruncate(EmbedTextDetails.Truncate truncate) {
		this.truncate = truncate;
	}

}
