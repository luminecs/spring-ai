package org.springframework.ai.huggingface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.huggingface.api.TextGenerationInferenceApi;
import org.springframework.ai.huggingface.invoker.ApiClient;
import org.springframework.ai.huggingface.model.AllOfGenerateResponseDetails;
import org.springframework.ai.huggingface.model.CompatGenerateRequest;
import org.springframework.ai.huggingface.model.GenerateParameters;
import org.springframework.ai.huggingface.model.GenerateResponse;

public class HuggingfaceChatModel implements ChatModel {

	private final String apiToken;

	private ApiClient apiClient = new ApiClient();

	private ObjectMapper objectMapper = new ObjectMapper();

	private TextGenerationInferenceApi textGenApi = new TextGenerationInferenceApi();

	private int maxNewTokens = 1000;

	public HuggingfaceChatModel(final String apiToken, String basePath) {
		this.apiToken = apiToken;
		this.apiClient.setBasePath(basePath);
		this.apiClient.addDefaultHeader("Authorization", "Bearer " + this.apiToken);
		this.textGenApi.setApiClient(this.apiClient);
	}

	@Override
	public ChatResponse call(Prompt prompt) {
		CompatGenerateRequest compatGenerateRequest = new CompatGenerateRequest();
		compatGenerateRequest.setInputs(prompt.getContents());
		GenerateParameters generateParameters = new GenerateParameters();

		generateParameters.setMaxNewTokens(this.maxNewTokens);
		compatGenerateRequest.setParameters(generateParameters);
		List<GenerateResponse> generateResponses = this.textGenApi.compatGenerate(compatGenerateRequest);
		List<Generation> generations = new ArrayList<>();
		for (GenerateResponse generateResponse : generateResponses) {
			String generatedText = generateResponse.getGeneratedText();
			AllOfGenerateResponseDetails allOfGenerateResponseDetails = generateResponse.getDetails();
			Map<String, Object> detailsMap = this.objectMapper.convertValue(allOfGenerateResponseDetails,
					new TypeReference<Map<String, Object>>() {

					});
			Generation generation = new Generation(new AssistantMessage(generatedText, detailsMap));
			generations.add(generation);
		}
		return new ChatResponse(generations);
	}

	public int getMaxNewTokens() {
		return this.maxNewTokens;
	}

	public void setMaxNewTokens(int maxNewTokens) {
		this.maxNewTokens = maxNewTokens;
	}

}
