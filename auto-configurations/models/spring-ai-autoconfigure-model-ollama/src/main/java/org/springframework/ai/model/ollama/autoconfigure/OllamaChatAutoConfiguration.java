package org.springframework.ai.model.ollama.autoconfigure;

import io.micrometer.observation.ObservationRegistry;

import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.ai.model.tool.DefaultToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionEligibilityPredicate;
import org.springframework.ai.model.tool.autoconfigure.ToolCallingAutoConfiguration;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = { RestClientAutoConfiguration.class, ToolCallingAutoConfiguration.class })
@ConditionalOnClass(OllamaChatModel.class)
@ConditionalOnProperty(name = SpringAIModelProperties.CHAT_MODEL, havingValue = SpringAIModels.OLLAMA,
		matchIfMissing = true)
@EnableConfigurationProperties({ OllamaChatProperties.class, OllamaInitializationProperties.class })
@ImportAutoConfiguration(classes = { OllamaApiAutoConfiguration.class, RestClientAutoConfiguration.class,
		ToolCallingAutoConfiguration.class, WebClientAutoConfiguration.class })
public class OllamaChatAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public OllamaChatModel ollamaChatModel(OllamaApi ollamaApi, OllamaChatProperties properties,
			OllamaInitializationProperties initProperties, ToolCallingManager toolCallingManager,
			ObjectProvider<ObservationRegistry> observationRegistry,
			ObjectProvider<ChatModelObservationConvention> observationConvention,
			ObjectProvider<ToolExecutionEligibilityPredicate> ollamaToolExecutionEligibilityPredicate) {
		var chatModelPullStrategy = initProperties.getChat().isInclude() ? initProperties.getPullModelStrategy()
				: PullModelStrategy.NEVER;

		var chatModel = OllamaChatModel.builder()
			.ollamaApi(ollamaApi)
			.defaultOptions(properties.getOptions())
			.toolCallingManager(toolCallingManager)
			.toolExecutionEligibilityPredicate(
					ollamaToolExecutionEligibilityPredicate.getIfUnique(DefaultToolExecutionEligibilityPredicate::new))
			.observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
			.modelManagementOptions(
					new ModelManagementOptions(chatModelPullStrategy, initProperties.getChat().getAdditionalModels(),
							initProperties.getTimeout(), initProperties.getMaxRetries()))
			.build();

		observationConvention.ifAvailable(chatModel::setObservationConvention);

		return chatModel;
	}

}
