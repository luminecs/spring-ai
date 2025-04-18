package org.springframework.ai.huggingface.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.huggingface.HuggingfaceChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "HUGGINGFACE_API_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "HUGGINGFACE_CHAT_URL", matches = ".+")
public class ClientIT {

	@Autowired
	protected HuggingfaceChatModel huggingfaceChatModel;

	@Test
	void helloWorldCompletion() {
		String mistral7bInstruct = """
				[INST] You are a helpful code assistant. Your task is to generate a valid JSON object based on the given information:
				name: John
				lastname: Smith
				address: #1 Samuel St.
				Just generate the JSON object without explanations:
				Your response should be in JSON format.
				Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
				Do not include markdown code blocks in your response.
				Remove the ```json markdown from the output.
				[/INST]
				""";
		Prompt prompt = new Prompt(mistral7bInstruct);
		ChatResponse chatResponse = this.huggingfaceChatModel.call(prompt);
		assertThat(chatResponse.getResult().getOutput().getText()).isNotEmpty();
		String expectedResponse = """
				{
				  "name": "John",
				  "lastname": "Smith",
				  "address": "#1 Samuel St."
				}""";
		assertThat(chatResponse.getResult().getOutput().getText()).isEqualTo(expectedResponse);
		assertThat(chatResponse.getResult().getOutput().getMetadata()).containsKey("generated_tokens");
		assertThat(chatResponse.getResult().getOutput().getMetadata()).containsEntry("generated_tokens", 32);

	}

}
