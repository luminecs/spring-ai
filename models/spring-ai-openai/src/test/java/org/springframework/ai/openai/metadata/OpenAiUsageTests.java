package org.springframework.ai.openai.metadata;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.metadata.DefaultUsage;
import org.springframework.ai.openai.api.OpenAiApi;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiUsageTests {

	private DefaultUsage getDefaultUsage(OpenAiApi.Usage usage) {
		return new DefaultUsage(usage.promptTokens(), usage.completionTokens(), usage.totalTokens(), usage);
	}

	@Test
	void whenPromptTokensIsPresent() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		assertThat(usage.getPromptTokens()).isEqualTo(200);
	}

	@Test
	void whenPromptTokensIsNull() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, null, 100);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		assertThat(usage.getPromptTokens()).isEqualTo(0);
	}

	@Test
	void whenGenerationTokensIsPresent() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		assertThat(usage.getCompletionTokens()).isEqualTo(100);
	}

	@Test
	void whenGenerationTokensIsNull() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(null, 200, 200);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		assertThat(usage.getCompletionTokens()).isEqualTo(0);
	}

	@Test
	void whenTotalTokensIsPresent() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		assertThat(usage.getTotalTokens()).isEqualTo(300);
	}

	@Test
	void whenTotalTokensIsNull() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		assertThat(usage.getTotalTokens()).isEqualTo(300);
	}

	@Test
	void whenPromptAndCompletionTokensDetailsIsNull() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300, null, null, null, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		assertThat(usage.getTotalTokens()).isEqualTo(300);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.promptTokensDetails()).isNull();
		assertThat(nativeUsage.completionTokenDetails()).isNull();
	}

	@Test
	void whenCompletionTokenDetailsIsNull() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300, null, null, null, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		assertThat(usage.getTotalTokens()).isEqualTo(300);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.completionTokenDetails()).isNull();
	}

	@Test
	void whenReasoningTokensIsNull() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300, null,
				new OpenAiApi.Usage.CompletionTokenDetails(null, null, null, null), null, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.completionTokenDetails().reasoningTokens()).isEqualTo(null);
	}

	@Test
	void whenCompletionTokenDetailsIsPresent() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300, null,
				new OpenAiApi.Usage.CompletionTokenDetails(50, null, null, null), null, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.completionTokenDetails().reasoningTokens()).isEqualTo(50);
		assertThat(nativeUsage.completionTokenDetails().acceptedPredictionTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().audioTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().rejectedPredictionTokens()).isEqualTo(null);
	}

	@Test
	void whenAcceptedPredictionTokensIsPresent() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300, null,
				new OpenAiApi.Usage.CompletionTokenDetails(null, 75, null, null), null, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.completionTokenDetails().reasoningTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().acceptedPredictionTokens()).isEqualTo(75);
		assertThat(nativeUsage.completionTokenDetails().audioTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().rejectedPredictionTokens()).isEqualTo(null);
	}

	@Test
	void whenAudioTokensIsPresent() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300, null,
				new OpenAiApi.Usage.CompletionTokenDetails(null, null, 125, null), null, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.completionTokenDetails().reasoningTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().acceptedPredictionTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().audioTokens()).isEqualTo(125);
		assertThat(nativeUsage.completionTokenDetails().rejectedPredictionTokens()).isEqualTo(null);
	}

	@Test
	void whenRejectedPredictionTokensIsNull() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300, null,
				new OpenAiApi.Usage.CompletionTokenDetails(null, null, null, null), null, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.completionTokenDetails().reasoningTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().acceptedPredictionTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().audioTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().rejectedPredictionTokens()).isEqualTo(null);
		assertThat(nativeUsage.promptTokensDetails()).isEqualTo(null);

	}

	@Test
	void whenRejectedPredictionTokensIsPresent() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300, null,
				new OpenAiApi.Usage.CompletionTokenDetails(null, null, null, 25), null, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.completionTokenDetails().reasoningTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().acceptedPredictionTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().audioTokens()).isEqualTo(null);
		assertThat(nativeUsage.completionTokenDetails().rejectedPredictionTokens()).isEqualTo(25);
	}

	@Test
	void whenCacheTokensIsNull() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300,
				new OpenAiApi.Usage.PromptTokensDetails(null, null), null, null, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.promptTokensDetails().audioTokens()).isEqualTo(null);
		assertThat(nativeUsage.promptTokensDetails().cachedTokens()).isEqualTo(null);
	}

	@Test
	void whenCacheTokensIsPresent() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300,
				new OpenAiApi.Usage.PromptTokensDetails(99, 15), null, null, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.promptTokensDetails().audioTokens()).isEqualTo(99);
		assertThat(nativeUsage.promptTokensDetails().cachedTokens()).isEqualTo(15);
	}

	@Test
	void whenPromptCacheHitTokensIsPresent() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300,
				new OpenAiApi.Usage.PromptTokensDetails(99, 15), null, 150, null);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.promptTokensDetails().audioTokens()).isEqualTo(99);
		assertThat(nativeUsage.promptTokensDetails().cachedTokens()).isEqualTo(15);
		assertThat(nativeUsage.promptCacheHitTokens()).isEqualTo(150);
		assertThat(nativeUsage.promptCacheMissTokens()).isNull();
	}

	@Test
	void whenPromptCacheMissTokensIsPresent() {
		OpenAiApi.Usage openAiUsage = new OpenAiApi.Usage(100, 200, 300,
				new OpenAiApi.Usage.PromptTokensDetails(99, 15), null, null, 80);
		DefaultUsage usage = getDefaultUsage(openAiUsage);
		OpenAiApi.Usage nativeUsage = (OpenAiApi.Usage) usage.getNativeUsage();
		assertThat(nativeUsage.promptTokensDetails().audioTokens()).isEqualTo(99);
		assertThat(nativeUsage.promptTokensDetails().cachedTokens()).isEqualTo(15);
		assertThat(nativeUsage.promptCacheMissTokens()).isEqualTo(80);
		assertThat(nativeUsage.promptCacheHitTokens()).isNull();
	}

}
