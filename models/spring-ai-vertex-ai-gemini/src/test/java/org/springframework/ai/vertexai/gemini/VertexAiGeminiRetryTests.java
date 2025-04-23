package org.springframework.ai.vertexai.gemini;

import java.io.IOException;
import java.util.List;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
public class VertexAiGeminiRetryTests {

	private TestRetryListener retryListener;

	private RetryTemplate retryTemplate;

	@Mock
	private VertexAI vertexAI;

	@Mock
	private GenerativeModel mockGenerativeModel;

	private TestVertexAiGeminiChatModel chatModel;

	@BeforeEach
	public void setUp() {
		this.retryTemplate = RetryUtils.SHORT_RETRY_TEMPLATE;
		this.retryListener = new TestRetryListener();
		this.retryTemplate.registerListener(this.retryListener);

		this.chatModel = new TestVertexAiGeminiChatModel(this.vertexAI,
				VertexAiGeminiChatOptions.builder()
					.temperature(0.7)
					.topP(1.0)
					.model(VertexAiGeminiChatModel.ChatModel.GEMINI_2_0_FLASH.getValue())
					.build(),
				this.retryTemplate);

		this.chatModel.setMockGenerativeModel(this.mockGenerativeModel);
	}

	@Test
	public void vertexAiGeminiChatTransientError() throws IOException {

		GenerateContentResponse mockedResponse = GenerateContentResponse.newBuilder()
			.addCandidates(Candidate.newBuilder()
				.setContent(Content.newBuilder().addParts(Part.newBuilder().setText("Response").build()).build())
				.build())
			.build();

		given(this.mockGenerativeModel.generateContent(any(List.class)))
			.willThrow(new TransientAiException("Transient Error 1"))
			.willThrow(new TransientAiException("Transient Error 2"))
			.willReturn(mockedResponse);

		ChatResponse result = this.chatModel.call(new Prompt("test prompt"));

		assertThat(result).isNotNull();
		assertThat(result.getResult().getOutput().getText()).isEqualTo("Response");
		assertThat(this.retryListener.onSuccessRetryCount).isEqualTo(2);
		assertThat(this.retryListener.onErrorRetryCount).isEqualTo(2);
	}

	@Test
	public void vertexAiGeminiChatNonTransientError() throws Exception {

		given(this.mockGenerativeModel.generateContent(any(List.class)))
			.willThrow(new RuntimeException("Non Transient Error"));

		assertThrows(RuntimeException.class, () -> this.chatModel.call(new Prompt("test prompt")));
	}

	private static class TestRetryListener implements RetryListener {

		int onErrorRetryCount = 0;

		int onSuccessRetryCount = 0;

		@Override
		public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
			this.onSuccessRetryCount = context.getRetryCount();
		}

		@Override
		public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
				Throwable throwable) {
			this.onErrorRetryCount = context.getRetryCount();
		}

	}

}
