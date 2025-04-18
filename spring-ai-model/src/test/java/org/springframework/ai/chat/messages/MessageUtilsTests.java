package org.springframework.ai.chat.messages;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MessageUtilsTests {

	@Test
	void readResource() {
		String content = MessageUtils.readResource(new ClassPathResource("prompt-user.txt"));
		assertThat(content).isEqualTo("Hello, world!");
	}

	@Test
	void readResourceWhenNull() {
		assertThatThrownBy(() -> MessageUtils.readResource(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("resource cannot be null");
	}

	@Test
	void readResourceWithCharset() {
		String content = MessageUtils.readResource(new ClassPathResource("prompt-user.txt"), StandardCharsets.UTF_8);
		assertThat(content).isEqualTo("Hello, world!");
	}

	@Test
	void readResourceWithCharsetWhenNull() {
		assertThatThrownBy(() -> MessageUtils.readResource(new ClassPathResource("prompt-user.txt"), null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("charset cannot be null");
	}

}
