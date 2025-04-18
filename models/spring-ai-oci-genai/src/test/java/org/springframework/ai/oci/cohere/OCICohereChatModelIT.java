package org.springframework.ai.oci.cohere;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.oci.BaseOCIGenAITest;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = org.springframework.ai.oci.BaseOCIGenAITest.OCI_COMPARTMENT_ID_KEY,
		matches = ".+")
@EnabledIfEnvironmentVariable(named = org.springframework.ai.oci.BaseOCIGenAITest.OCI_CHAT_MODEL_ID_KEY, matches = ".+")
public class OCICohereChatModelIT extends BaseOCIGenAITest {

	private static final ChatModel chatModel = new OCICohereChatModel(getGenerativeAIClient(), options().build());

	@Test
	void chatSimple() {
		String response = chatModel.call("Tell me a random fact about Canada");
		assertThat(response).isNotBlank();
	}

	@Test
	void chatMessages() {
		String response = chatModel.call(new UserMessage("Tell me a random fact about the Arctic Circle"),
				new SystemMessage("You are a helpful assistant"));
		assertThat(response).isNotBlank();
	}

	@Test
	void chatPrompt() {
		ChatResponse response = chatModel.call(new Prompt("What's the difference between Top P and Top K sampling?"));
		assertThat(response).isNotNull();
		assertThat(response.getMetadata().getModel()).isEqualTo(CHAT_MODEL_ID);
		assertThat(response.getResult()).isNotNull();
		assertThat(response.getResult().getOutput().getText()).isNotBlank();
	}

}
