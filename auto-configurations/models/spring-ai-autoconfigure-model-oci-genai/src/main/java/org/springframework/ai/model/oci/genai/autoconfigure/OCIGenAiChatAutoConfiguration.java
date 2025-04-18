package org.springframework.ai.model.oci.genai.autoconfigure;

import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.oci.cohere.OCICohereChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(OCICohereChatModel.class)
@EnableConfigurationProperties(OCICohereChatModelProperties.class)
@ConditionalOnProperty(name = SpringAIModelProperties.CHAT_MODEL, havingValue = SpringAIModels.OCI_GENAI,
		matchIfMissing = true)
@ImportAutoConfiguration(OCIGenAiInferenceClientAutoConfiguration.class)
public class OCIGenAiChatAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public OCICohereChatModel ociChatModel(GenerativeAiInferenceClient generativeAiClient,
			OCICohereChatModelProperties properties, ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<ChatModelObservationConvention> observationConvention) {
		var chatModel = new OCICohereChatModel(generativeAiClient, properties.getOptions(),
				observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP));
		observationConvention.ifAvailable(chatModel::setObservationConvention);

		return chatModel;
	}

}
