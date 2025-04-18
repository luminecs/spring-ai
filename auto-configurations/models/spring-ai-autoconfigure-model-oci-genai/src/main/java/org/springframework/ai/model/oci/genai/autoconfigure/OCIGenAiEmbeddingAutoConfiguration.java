package org.springframework.ai.model.oci.genai.autoconfigure;

import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;

import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.oci.OCIEmbeddingModel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(OCIEmbeddingModel.class)
@EnableConfigurationProperties(OCIEmbeddingModelProperties.class)
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.OCI_GENAI,
		matchIfMissing = true)
@ImportAutoConfiguration(OCIGenAiInferenceClientAutoConfiguration.class)
public class OCIGenAiEmbeddingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public OCIEmbeddingModel ociEmbeddingModel(GenerativeAiInferenceClient generativeAiClient,
			OCIEmbeddingModelProperties properties) {
		return new OCIEmbeddingModel(generativeAiClient, properties.getEmbeddingOptions());
	}

}
