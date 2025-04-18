package org.springframework.ai.model.huggingface.autoconfigure;

import org.springframework.ai.huggingface.HuggingfaceChatModel;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(HuggingfaceChatModel.class)
@EnableConfigurationProperties(HuggingfaceChatProperties.class)
@ConditionalOnProperty(name = SpringAIModelProperties.CHAT_MODEL, havingValue = SpringAIModels.HUGGINGFACE,
		matchIfMissing = true)
public class HuggingfaceChatAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public HuggingfaceChatModel huggingfaceChatModel(HuggingfaceChatProperties huggingfaceChatProperties) {
		return new HuggingfaceChatModel(huggingfaceChatProperties.getApiKey(), huggingfaceChatProperties.getUrl());
	}

}
