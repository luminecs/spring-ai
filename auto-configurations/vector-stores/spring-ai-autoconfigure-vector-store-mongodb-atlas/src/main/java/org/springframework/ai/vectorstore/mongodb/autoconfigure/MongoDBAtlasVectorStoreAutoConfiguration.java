package org.springframework.ai.vectorstore.mongodb.autoconfigure;

import java.util.Arrays;
import java.util.List;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.vectorstore.SpringAIVectorStoreTypes;
import org.springframework.ai.vectorstore.mongodb.atlas.MongoDBAtlasVectorStore;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationConvention;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;

@AutoConfiguration
@ConditionalOnClass({ MongoDBAtlasVectorStore.class, EmbeddingModel.class, MongoTemplate.class })
@EnableConfigurationProperties(MongoDBAtlasVectorStoreProperties.class)
@ConditionalOnProperty(name = SpringAIVectorStoreTypes.TYPE, havingValue = SpringAIVectorStoreTypes.MONGODB_ATLAS,
		matchIfMissing = true)
public class MongoDBAtlasVectorStoreAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean(BatchingStrategy.class)
	BatchingStrategy batchingStrategy() {
		return new TokenCountBatchingStrategy();
	}

	@Bean
	@ConditionalOnMissingBean
	MongoDBAtlasVectorStore vectorStore(MongoTemplate mongoTemplate, EmbeddingModel embeddingModel,
			MongoDBAtlasVectorStoreProperties properties, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<VectorStoreObservationConvention> customObservationConvention,
			BatchingStrategy batchingStrategy) {

		MongoDBAtlasVectorStore.Builder builder = MongoDBAtlasVectorStore.builder(mongoTemplate, embeddingModel)
			.initializeSchema(properties.isInitializeSchema())
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.customObservationConvention(customObservationConvention.getIfAvailable(() -> null))
			.batchingStrategy(batchingStrategy);

		PropertyMapper mapper = PropertyMapper.get();
		mapper.from(properties::getCollectionName).whenHasText().to(builder::collectionName);
		mapper.from(properties::getPathName).whenHasText().to(builder::pathName);
		mapper.from(properties::getIndexName).whenHasText().to(builder::vectorIndexName);

		List<String> metadataFields = properties.getMetadataFieldsToFilter();
		if (!CollectionUtils.isEmpty(metadataFields)) {
			builder.metadataFieldsToFilter(metadataFields);
		}

		return builder.build();
	}

	@Bean
	public Converter<MimeType, String> mimeTypeToStringConverter() {
		return new Converter<MimeType, String>() {

			@Override
			public String convert(MimeType source) {
				return source.toString();
			}
		};
	}

	@Bean
	public Converter<String, MimeType> stringToMimeTypeConverter() {
		return new Converter<String, MimeType>() {

			@Override
			public MimeType convert(String source) {
				return MimeType.valueOf(source);
			}
		};
	}

	@Bean
	public MongoCustomConversions mongoCustomConversions() {
		return new MongoCustomConversions(Arrays.asList(mimeTypeToStringConverter(), stringToMimeTypeConverter()));
	}

}
