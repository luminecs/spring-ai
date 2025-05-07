package org.springframework.ai.chat.client;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class DefaultChatClientBuilderTests {

	@Test
	void whenCloneBuilder() {
		var chatModel = mock(ChatModel.class);
		var originalBuilder = new DefaultChatClientBuilder(chatModel);
		originalBuilder.defaultSystem("first instructions");
		var clonedBuilder = (DefaultChatClientBuilder) originalBuilder.clone();
		originalBuilder.defaultSystem("second instructions");

		assertThat(clonedBuilder).isNotSameAs(originalBuilder);
		var clonedBuilderRequestSpec = (DefaultChatClient.DefaultChatClientRequestSpec) ReflectionTestUtils
			.getField(clonedBuilder, "defaultRequest");
		assertThat(clonedBuilderRequestSpec).isNotNull();
		assertThat(clonedBuilderRequestSpec.getSystemText()).isEqualTo("first instructions");
	}

	@Test
	void whenChatModelIsNullThenThrows() {
		assertThatThrownBy(() -> new DefaultChatClientBuilder(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("the org.springframework.ai.chat.model.ChatModel must be non-null");
	}

	@Test
	void whenObservationRegistryIsNullThenThrows() {
		assertThatThrownBy(() -> new DefaultChatClientBuilder(mock(ChatModel.class), null, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("the io.micrometer.observation.ObservationRegistry must be non-null");
	}

	@Test
	void whenUserResourceIsNullThenThrows() {
		DefaultChatClientBuilder builder = new DefaultChatClientBuilder(mock(ChatModel.class));
		assertThatThrownBy(() -> builder.defaultUser(null, Charset.defaultCharset()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("text cannot be null");
	}

	@Test
	void whenUserCharsetIsNullThenThrows() {
		DefaultChatClientBuilder builder = new DefaultChatClientBuilder(mock(ChatModel.class));
		assertThatThrownBy(() -> builder.defaultUser(new ClassPathResource("user-prompt.txt"), null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("charset cannot be null");
	}

	@Test
	void whenSystemResourceIsNullThenThrows() {
		DefaultChatClientBuilder builder = new DefaultChatClientBuilder(mock(ChatModel.class));
		assertThatThrownBy(() -> builder.defaultSystem(null, Charset.defaultCharset()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("text cannot be null");
	}

	@Test
	void whenSystemCharsetIsNullThenThrows() {
		DefaultChatClientBuilder builder = new DefaultChatClientBuilder(mock(ChatModel.class));
		assertThatThrownBy(() -> builder.defaultSystem(new ClassPathResource("system-prompt.txt"), null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("charset cannot be null");
	}

	@Test
	void whenTemplateRendererIsNullThenThrows() {
		DefaultChatClientBuilder builder = new DefaultChatClientBuilder(mock(ChatModel.class));
		assertThatThrownBy(() -> builder.defaultTemplateRenderer(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("templateRenderer cannot be null");
	}

}
