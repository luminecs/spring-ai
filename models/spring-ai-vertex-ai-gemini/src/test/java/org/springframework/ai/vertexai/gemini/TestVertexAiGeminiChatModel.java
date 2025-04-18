package org.springframework.ai.vertexai.gemini;

import java.io.IOException;
import java.util.List;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;

import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackResolver;
import org.springframework.retry.support.RetryTemplate;

public class TestVertexAiGeminiChatModel extends VertexAiGeminiChatModel {

	private GenerativeModel mockGenerativeModel;

	public TestVertexAiGeminiChatModel(VertexAI vertexAI, VertexAiGeminiChatOptions options,
			FunctionCallbackResolver functionCallbackResolver, List<FunctionCallback> toolFunctionCallbacks,
			RetryTemplate retryTemplate) {
		super(vertexAI, options, functionCallbackResolver, toolFunctionCallbacks, retryTemplate);
	}

	@Override
	GenerateContentResponse getContentResponse(GeminiRequest request) {
		if (this.mockGenerativeModel != null) {
			try {
				return this.mockGenerativeModel.generateContent(request.contents());
			}
			catch (IOException e) {

				throw new RuntimeException("Failed to generate content", e);
			}
			catch (RuntimeException e) {

				throw e;
			}
		}
		return super.getContentResponse(request);
	}

	public void setMockGenerativeModel(GenerativeModel mockGenerativeModel) {
		this.mockGenerativeModel = mockGenerativeModel;
	}

}
