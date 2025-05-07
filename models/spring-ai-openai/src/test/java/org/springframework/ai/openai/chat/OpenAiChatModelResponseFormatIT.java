package org.springframework.ai.openai.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiApi.ChatModel;
import org.springframework.ai.openai.api.ResponseFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = OpenAiChatModelResponseFormatIT.Config.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class OpenAiChatModelResponseFormatIT {

	private static ObjectMapper MAPPER = new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private OpenAiChatModel openAiChatModel;

	public static boolean isValidJson(String json) {
		try {
			MAPPER.readTree(json);
		}
		catch (JacksonException e) {
			return false;
		}
		return true;
	}

	@Test
	void jsonObject() throws JsonMappingException, JsonProcessingException {

		Prompt prompt = new Prompt("List 8 planets. Use JSON response",
				OpenAiChatOptions.builder()
					.responseFormat(ResponseFormat.builder().type(ResponseFormat.Type.JSON_OBJECT).build())
					.build());

		ChatResponse response = this.openAiChatModel.call(prompt);

		assertThat(response).isNotNull();

		String content = response.getResult().getOutput().getText();

		logger.info("Response content: {}", content);

		assertThat(isValidJson(content)).isTrue();
	}

	@Test
	void jsonSchema() throws JsonMappingException, JsonProcessingException {

		var jsonSchema = """
				{
					"type": "object",
					"properties": {
						"steps": {
							"type": "array",
							"items": {
								"type": "object",
								"properties": {
									"explanation": { "type": "string" },
									"output": { "type": "string" }
								},
								"required": ["explanation", "output"],
								"additionalProperties": false
							}
						},
						"final_answer": { "type": "string" }
					},
					"required": ["steps", "final_answer"],
					"additionalProperties": false
				}
				""";

		Prompt prompt = new Prompt("how can I solve 8x + 7 = -23",
				OpenAiChatOptions.builder()
					.model(ChatModel.GPT_4_O_MINI)
					.responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema))
					.build());

		ChatResponse response = this.openAiChatModel.call(prompt);

		assertThat(response).isNotNull();

		String content = response.getResult().getOutput().getText();

		logger.info("Response content: {}", content);

		assertThat(isValidJson(content)).isTrue();
	}

	@Test
	void jsonSchemaThroughIndividualSetters() throws JsonMappingException, JsonProcessingException {

		var jsonSchema = """
				{
					"type": "object",
					"properties": {
						"steps": {
							"type": "array",
							"items": {
								"type": "object",
								"properties": {
									"explanation": { "type": "string" },
									"output": { "type": "string" }
								},
								"required": ["explanation", "output"],
								"additionalProperties": false
							}
						},
						"final_answer": { "type": "string" }
					},
					"required": ["steps", "final_answer"],
					"additionalProperties": false
				}
				""";

		var responseFormat = new ResponseFormat();
		responseFormat.setType(ResponseFormat.Type.JSON_SCHEMA);
		responseFormat.setSchema(jsonSchema);
		Prompt prompt = new Prompt("how can I solve 8x + 7 = -23",
				OpenAiChatOptions.builder().model(ChatModel.GPT_4_O_MINI).responseFormat(responseFormat).build());

		ChatResponse response = this.openAiChatModel.call(prompt);

		assertThat(response).isNotNull();

		String content = response.getResult().getOutput().getText();

		logger.info("Response content: {}", content);

		assertThat(isValidJson(content)).isTrue();
	}

	@Test
	void jsonSchemaBeanConverter() throws JsonMappingException, JsonProcessingException {

		@JsonPropertyOrder({ "steps", "final_answer" })
		record MathReasoning(@JsonProperty(required = true, value = "steps") Steps steps,
				@JsonProperty(required = true, value = "final_answer") String finalAnswer) {

			record Steps(@JsonProperty(required = true, value = "items") Items[] items) {

				@JsonPropertyOrder({ "output", "explanation" })
				record Items(@JsonProperty(required = true, value = "explanation") String explanation,
						@JsonProperty(required = true, value = "output") String output) {

				}

			}

		}

		var outputConverter = new BeanOutputConverter<>(MathReasoning.class);
		// @formatter:off

		var expectedJsonSchema = """
				{
				  "$schema" : "https:
				  "type" : "object",
				  "properties" : {
				    "steps" : {
				      "type" : "object",
				      "properties" : {
				        "items" : {
				          "type" : "array",
				          "items" : {
				            "type" : "object",
				            "properties" : {
				              "output" : {
				                "type" : "string"
				              },
				              "explanation" : {
				                "type" : "string"
				              }
				            },
				            "required" : [ "output", "explanation" ],
				            "additionalProperties" : false
				          }
				        }
				      },
				      "required" : [ "items" ],
				      "additionalProperties" : false
				    },
				    "final_answer" : {
				      "type" : "string"
				    }
				  },
				  "required" : [ "steps", "final_answer" ],
				  "additionalProperties" : false
				}""";
		// @formatter:on
		// CHECKSTYLE:ON
		var jsonSchema1 = outputConverter.getJsonSchema();

		assertThat(jsonSchema1).isNotNull();
		assertThat(jsonSchema1).isEqualTo(expectedJsonSchema);

		Prompt prompt = new Prompt("how can I solve 8x + 7 = -23",
				OpenAiChatOptions.builder()
					.model(ChatModel.GPT_4_O_MINI)
					.responseFormat(new ResponseFormat(ResponseFormat.Type.JSON_SCHEMA, jsonSchema1))
					.build());

		ChatResponse response = this.openAiChatModel.call(prompt);

		assertThat(response).isNotNull();

		String content = response.getResult().getOutput().getText();

		logger.info("Response content: {}", content);

		assertThat(isValidJson(content)).isTrue();

		// Check if the order is correct as specified in the schema. Steps should come
		// first before final answer.
		assertThat(content.startsWith("{\"steps\":{\"items\":["));

		MathReasoning mathReasoning = outputConverter.convert(content);

		assertThat(mathReasoning).isNotNull();
		logger.info(mathReasoning.toString());
	}

	@SpringBootConfiguration
	static class Config {

		@Bean
		public OpenAiApi chatCompletionApi() {
			return OpenAiApi.builder().apiKey(System.getenv("OPENAI_API_KEY")).build();
		}

		@Bean
		public OpenAiChatModel openAiClient(OpenAiApi openAiApi) {
			return OpenAiChatModel.builder().openAiApi(openAiApi).build();
		}

	}

}
