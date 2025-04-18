package org.springframework.ai.vectorstore.mariadb.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore;
import org.springframework.ai.vectorstore.mariadb.MariaDBVectorStore.MariaDBDistanceType;

import static org.assertj.core.api.Assertions.assertThat;

public class MariaDbStorePropertiesTests {

	@Test
	public void defaultValues() {
		var props = new MariaDbStoreProperties();
		assertThat(props.getDimensions()).isEqualTo(MariaDBVectorStore.INVALID_EMBEDDING_DIMENSION);
		assertThat(props.getDistanceType()).isEqualTo(MariaDBDistanceType.COSINE);
		assertThat(props.isRemoveExistingVectorStoreTable()).isFalse();

		assertThat(props.isSchemaValidation()).isFalse();
		assertThat(props.getSchemaName()).isNull();
		assertThat(props.getTableName()).isEqualTo(MariaDBVectorStore.DEFAULT_TABLE_NAME);

	}

	@Test
	public void customValues() {
		var props = new MariaDbStoreProperties();

		props.setDimensions(1536);
		props.setDistanceType(MariaDBDistanceType.EUCLIDEAN);
		props.setRemoveExistingVectorStoreTable(true);

		props.setSchemaValidation(true);
		props.setSchemaName("my_vector_schema");
		props.setTableName("my_vector_table");
		props.setIdFieldName("my_vector_id");
		props.setMetadataFieldName("my_vector_meta");
		props.setContentFieldName("my_vector_content");
		props.setEmbeddingFieldName("my_vector_embedding");
		props.setInitializeSchema(true);

		assertThat(props.getDimensions()).isEqualTo(1536);
		assertThat(props.getDistanceType()).isEqualTo(MariaDBDistanceType.EUCLIDEAN);
		assertThat(props.isRemoveExistingVectorStoreTable()).isTrue();

		assertThat(props.isSchemaValidation()).isTrue();
		assertThat(props.getSchemaName()).isEqualTo("my_vector_schema");
		assertThat(props.getTableName()).isEqualTo("my_vector_table");
		assertThat(props.getIdFieldName()).isEqualTo("my_vector_id");
		assertThat(props.getMetadataFieldName()).isEqualTo("my_vector_meta");
		assertThat(props.getContentFieldName()).isEqualTo("my_vector_content");
		assertThat(props.getEmbeddingFieldName()).isEqualTo("my_vector_embedding");
	}

}
