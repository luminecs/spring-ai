package org.springframework.ai.minimax.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.minimax.api.MiniMaxApi.ChatCompletion;
import org.springframework.ai.minimax.api.MiniMaxApi.ChatCompletionMessage;
import org.springframework.ai.minimax.api.MiniMaxApi.ChatCompletionMessage.Role;
import org.springframework.ai.minimax.api.MiniMaxApi.ChatCompletionMessage.ToolCall;
import org.springframework.ai.minimax.api.MiniMaxApi.ChatCompletionRequest;
import org.springframework.ai.minimax.api.MiniMaxApi.ChatCompletionRequest.ToolChoiceBuilder;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "MINIMAX_API_KEY", matches = ".+")
public class MiniMaxApiToolFunctionCallIT {

	private final Logger logger = LoggerFactory.getLogger(MiniMaxApiToolFunctionCallIT.class);

	MockWeatherService weatherService = new MockWeatherService();

	MiniMaxApi miniMaxApi = new MiniMaxApi(System.getenv("MINIMAX_API_KEY"));

	private static <T> T fromJson(String json, Class<T> targetClass) {
		try {
			return new ObjectMapper().readValue(json, targetClass);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("null")
	@Test
	public void toolFunctionCall() {

		var message = new ChatCompletionMessage(
				"What's the weather like in San Francisco? Return the temperature in Celsius.", Role.USER);

		var functionTool = new MiniMaxApi.FunctionTool(MiniMaxApi.FunctionTool.Type.FUNCTION,
				new MiniMaxApi.FunctionTool.Function(
						"Get the weather in location. Return temperature in 30°F or 30°C format.", "getCurrentWeather",
						"""
								{
									"type": "object",
									"properties": {
										"location": {
											"type": "string",
											"description": "The city and state e.g. San Francisco, CA"
										},
										"lat": {
											"type": "number",
											"description": "The city latitude"
										},
										"lon": {
											"type": "number",
											"description": "The city longitude"
										},
										"unit": {
											"type": "string",
											"enum": ["C", "F"]
										}
									},
									"required": ["location", "lat", "lon", "unit"]
								}
								"""));

		List<ChatCompletionMessage> messages = new ArrayList<>(List.of(message));

		ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(messages,
				org.springframework.ai.minimax.api.MiniMaxApi.ChatModel.ABAB_6_5_Chat.getValue(), List.of(functionTool),
				ToolChoiceBuilder.AUTO);

		ResponseEntity<ChatCompletion> chatCompletion = this.miniMaxApi.chatCompletionEntity(chatCompletionRequest);

		assertThat(chatCompletion.getBody()).isNotNull();
		assertThat(chatCompletion.getBody().choices()).isNotEmpty();

		ChatCompletionMessage responseMessage = chatCompletion.getBody().choices().get(0).message();

		assertThat(responseMessage.role()).isEqualTo(Role.ASSISTANT);
		assertThat(responseMessage.toolCalls()).isNotNull();

		messages.add(responseMessage);

		for (ToolCall toolCall : responseMessage.toolCalls()) {
			var functionName = toolCall.function().name();
			if ("getCurrentWeather".equals(functionName)) {
				MockWeatherService.Request weatherRequest = fromJson(toolCall.function().arguments(),
						MockWeatherService.Request.class);

				MockWeatherService.Response weatherResponse = this.weatherService.apply(weatherRequest);

				messages.add(new ChatCompletionMessage("" + weatherResponse.temp() + weatherRequest.unit(), Role.TOOL,
						functionName, toolCall.id(), null));
			}
		}

		var functionResponseRequest = new ChatCompletionRequest(messages,
				org.springframework.ai.minimax.api.MiniMaxApi.ChatModel.ABAB_6_5_Chat.getValue(), 0.5);

		ResponseEntity<ChatCompletion> chatCompletion2 = this.miniMaxApi.chatCompletionEntity(functionResponseRequest);

		logger.info("Final response: " + chatCompletion2.getBody());

		assertThat(Objects.requireNonNull(chatCompletion2.getBody()).choices()).isNotEmpty();

		assertThat(chatCompletion2.getBody().choices().get(0).message().role()).isEqualTo(Role.ASSISTANT);
		assertThat(chatCompletion2.getBody().choices().get(0).message().content()).contains("San Francisco")
			.containsAnyOf("30.0°C", "30°C", "30.0")
			.containsAnyOf("°C", "Celsius");
	}

	@SuppressWarnings("null")
	@Test
	public void webSearchToolFunctionCall() {

		var message = new ChatCompletionMessage(
				"How many gold medals has the United States won in total at the 2024 Olympics?", Role.USER);

		var functionTool = MiniMaxApi.FunctionTool.webSearchFunctionTool();

		List<ChatCompletionMessage> messages = new ArrayList<>(List.of(message));

		ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest(messages,
				org.springframework.ai.minimax.api.MiniMaxApi.ChatModel.ABAB_6_5_S_Chat.getValue(),
				List.of(functionTool), ToolChoiceBuilder.AUTO);

		ResponseEntity<ChatCompletion> chatCompletion = this.miniMaxApi.chatCompletionEntity(chatCompletionRequest);

		assertThat(chatCompletion.getBody()).isNotNull();
		assertThat(chatCompletion.getBody().choices()).isNotEmpty();

		List<ChatCompletionMessage> responseMessages = chatCompletion.getBody().choices().get(0).messages();
		ChatCompletionMessage assistantMessage = responseMessages.get(responseMessages.size() - 1);

		assertThat(assistantMessage.role()).isEqualTo(Role.ASSISTANT);
		assertThat(assistantMessage.content()).contains("40");
	}

}
