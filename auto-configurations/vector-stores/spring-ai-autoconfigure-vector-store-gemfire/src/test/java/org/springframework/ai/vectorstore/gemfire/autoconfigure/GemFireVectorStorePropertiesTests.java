package org.springframework.ai.vectorstore.gemfire.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.vectorstore.gemfire.GemFireVectorStore;

import static org.assertj.core.api.Assertions.assertThat;

class GemFireVectorStorePropertiesTests {

	@Test
	void defaultValues() {
		var props = new GemFireVectorStoreProperties();
		assertThat(props.getIndexName()).isEqualTo(GemFireVectorStore.DEFAULT_INDEX_NAME);
		assertThat(props.getHost()).isEqualTo(GemFireVectorStore.DEFAULT_HOST);
		assertThat(props.getPort()).isEqualTo(GemFireVectorStore.DEFAULT_PORT);
		assertThat(props.getBeamWidth()).isEqualTo(GemFireVectorStore.DEFAULT_BEAM_WIDTH);
		assertThat(props.getMaxConnections()).isEqualTo(GemFireVectorStore.DEFAULT_MAX_CONNECTIONS);
		assertThat(props.getFields()).isEqualTo(GemFireVectorStore.DEFAULT_FIELDS);
		assertThat(props.getBuckets()).isEqualTo(GemFireVectorStore.DEFAULT_BUCKETS);
	}

	@Test
	void customValues() {
		var props = new GemFireVectorStoreProperties();
		props.setIndexName("spring-ai-index");
		props.setHost("localhost");
		props.setPort(9090);
		props.setBeamWidth(10);
		props.setMaxConnections(10);
		props.setFields(new String[] { "test" });
		props.setBuckets(10);

		assertThat(props.getIndexName()).isEqualTo("spring-ai-index");
		assertThat(props.getHost()).isEqualTo("localhost");
		assertThat(props.getPort()).isEqualTo(9090);
		assertThat(props.getBeamWidth()).isEqualTo(10);
		assertThat(props.getMaxConnections()).isEqualTo(10);
		assertThat(props.getFields()).isEqualTo(new String[] { "test" });
		assertThat(props.getBuckets()).isEqualTo(10);

	}

}
