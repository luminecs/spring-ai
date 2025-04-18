package org.springframework.ai.model.transformers.autoconfigure;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.onnxruntime.OrtSession;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties({ TransformersEmbeddingModelProperties.class })
@ConditionalOnProperty(name = SpringAIModelProperties.EMBEDDING_MODEL, havingValue = SpringAIModels.TRANSFORMERS,
		matchIfMissing = true)
@ConditionalOnClass({ OrtSession.class, HuggingFaceTokenizer.class, TransformersEmbeddingModel.class })
public class TransformersEmbeddingModelAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public TransformersEmbeddingModel embeddingModel(TransformersEmbeddingModelProperties properties,
			ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<EmbeddingModelObservationConvention> observationConvention) {

		TransformersEmbeddingModel embeddingModel = new TransformersEmbeddingModel(properties.getMetadataMode(),
				observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP));

		embeddingModel.setDisableCaching(!properties.getCache().isEnabled());
		embeddingModel.setResourceCacheDirectory(properties.getCache().getDirectory());

		embeddingModel.setTokenizerResource(properties.getTokenizer().getUri());
		embeddingModel.setTokenizerOptions(properties.getTokenizer().getOptions());

		embeddingModel.setModelResource(properties.getOnnx().getModelUri());

		embeddingModel.setGpuDeviceId(properties.getOnnx().getGpuDeviceId());

		embeddingModel.setModelOutputName(properties.getOnnx().getModelOutputName());

		observationConvention.ifAvailable(embeddingModel::setObservationConvention);

		return embeddingModel;
	}

}
