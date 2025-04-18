package org.springframework.ai.vectorstore.redis.autoconfigure;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RedisVectorStorePropertiesTests {

	@Test
	void defaultValues() {
		var props = new RedisVectorStoreProperties();
		assertThat(props.getIndexName()).isEqualTo("default-index");
		assertThat(props.getPrefix()).isEqualTo("default:");
	}

	@Test
	void customValues() {
		var props = new RedisVectorStoreProperties();
		props.setIndexName("myIdx");
		props.setPrefix("doc:");

		assertThat(props.getIndexName()).isEqualTo("myIdx");
		assertThat(props.getPrefix()).isEqualTo("doc:");
	}

}
