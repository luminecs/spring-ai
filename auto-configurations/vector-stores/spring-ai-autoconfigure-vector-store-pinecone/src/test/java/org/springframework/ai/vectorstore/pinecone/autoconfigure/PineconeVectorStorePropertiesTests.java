package org.springframework.ai.vectorstore.pinecone.autoconfigure;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import org.springframework.ai.document.DocumentMetadata;
import org.springframework.ai.vectorstore.pinecone.PineconeVectorStore;

import static org.assertj.core.api.Assertions.assertThat;

public class PineconeVectorStorePropertiesTests {

	@Test
	public void defaultValues() {
		var props = new PineconeVectorStoreProperties();
		assertThat(props.getEnvironment()).isEqualTo("gcp-starter");
		assertThat(props.getNamespace()).isEqualTo("");
		assertThat(props.getApiKey()).isNull();
		assertThat(props.getProjectId()).isNull();
		assertThat(props.getIndexName()).isNull();
		assertThat(props.getServerSideTimeout()).isEqualTo(Duration.ofSeconds(20));
		assertThat(props.getContentFieldName()).isEqualTo(PineconeVectorStore.CONTENT_FIELD_NAME);
		assertThat(props.getDistanceMetadataFieldName()).isEqualTo(DocumentMetadata.DISTANCE.value());
	}

	@Test
	public void customValues() {
		var props = new PineconeVectorStoreProperties();
		props.setApiKey("key");
		props.setEnvironment("env");
		props.setIndexName("index");
		props.setNamespace("namespace");
		props.setProjectId("project");
		props.setServerSideTimeout(Duration.ofSeconds(60));
		props.setContentFieldName("article");
		props.setDistanceMetadataFieldName("distance2");

		assertThat(props.getEnvironment()).isEqualTo("env");
		assertThat(props.getNamespace()).isEqualTo("namespace");
		assertThat(props.getApiKey()).isEqualTo("key");
		assertThat(props.getProjectId()).isEqualTo("project");
		assertThat(props.getIndexName()).isEqualTo("index");
		assertThat(props.getServerSideTimeout()).isEqualTo(Duration.ofSeconds(60));
		assertThat(props.getContentFieldName()).isEqualTo("article");
		assertThat(props.getDistanceMetadataFieldName()).isEqualTo("distance2");
	}

}
