package org.springframework.ai.oci;

public class BaseEmbeddingModelTest extends BaseOCIGenAITest {

	public static final String EMBEDDING_MODEL_V2 = "cohere.embed-english-light-v2.0";

	public static final String EMBEDDING_MODEL_V3 = "cohere.embed-english-v3.0";

	public static OCIEmbeddingModel getEmbeddingModel() {
		OCIEmbeddingOptions options = OCIEmbeddingOptions.builder()
			.model(EMBEDDING_MODEL_V2)
			.compartment(COMPARTMENT_ID)
			.servingMode("on-demand")
			.build();
		return new OCIEmbeddingModel(getGenerativeAIClient(), options);
	}

}
