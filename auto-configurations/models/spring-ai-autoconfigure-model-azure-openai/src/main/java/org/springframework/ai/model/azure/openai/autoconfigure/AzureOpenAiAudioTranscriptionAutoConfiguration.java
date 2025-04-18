package org.springframework.ai.model.azure.openai.autoconfigure;

import com.azure.ai.openai.OpenAIClientBuilder;

import org.springframework.ai.azure.openai.AzureOpenAiAudioTranscriptionModel;
import org.springframework.ai.model.SpringAIModelProperties;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass({ AzureOpenAiAudioTranscriptionModel.class })
@EnableConfigurationProperties(AzureOpenAiAudioTranscriptionProperties.class)
@ConditionalOnProperty(name = SpringAIModelProperties.AUDIO_TRANSCRIPTION_MODEL,
		havingValue = SpringAIModels.AZURE_OPENAI, matchIfMissing = true)
@Import(AzureOpenAiClientBuilderConfiguration.class)
public class AzureOpenAiAudioTranscriptionAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public AzureOpenAiAudioTranscriptionModel azureOpenAiAudioTranscriptionModel(OpenAIClientBuilder openAIClient,
			AzureOpenAiAudioTranscriptionProperties audioProperties) {
		return new AzureOpenAiAudioTranscriptionModel(openAIClient.buildClient(), audioProperties.getOptions());
	}

}
