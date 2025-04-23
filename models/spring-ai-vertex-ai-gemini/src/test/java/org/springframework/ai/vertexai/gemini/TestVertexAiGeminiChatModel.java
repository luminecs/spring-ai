package org.springframework.ai.vertexai.gemini;

import java.io.IOException;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;

import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.retry.support.RetryTemplate;

public class TestVertexAiGeminiChatModel extends VertexAiGeminiChatModel {

	private GenerativeModel mockGenerativeModel;

	public TestVertexAiGeminiChatModel(VertexAI vertexAI, VertexAiGeminiChatOptions options,
			RetryTemplate retryTemplate) {
		super(vertexAI, options, ToolCallingManager.builder().build(), retryTemplate, null);
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
