package org.springframework.ai.vectorstore.oracle.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.ai.vectorstore.oracle.OracleVectorStore;
import org.springframework.ai.vectorstore.oracle.OracleVectorStore.OracleVectorStoreDistanceType;
import org.springframework.ai.vectorstore.oracle.OracleVectorStore.OracleVectorStoreIndexType;

import static org.assertj.core.api.Assertions.assertThat;

public class OracleVectorStorePropertiesTests {

	@Test
	public void defaultValues() {
		var props = new OracleVectorStoreProperties();
		assertThat(props.getDimensions()).isEqualTo(OracleVectorStore.DEFAULT_DIMENSIONS);
		assertThat(props.getDistanceType()).isEqualTo(OracleVectorStoreDistanceType.COSINE);
		assertThat(props.getIndexType()).isEqualTo(OracleVectorStoreIndexType.IVF);
		assertThat(props.isRemoveExistingVectorStoreTable()).isFalse();
	}

	@Test
	public void customValues() {
		var props = new OracleVectorStoreProperties();

		props.setDimensions(1536);
		props.setDistanceType(OracleVectorStoreDistanceType.EUCLIDEAN);
		props.setIndexType(OracleVectorStoreIndexType.IVF);
		props.setRemoveExistingVectorStoreTable(true);

		assertThat(props.getDimensions()).isEqualTo(1536);
		assertThat(props.getDistanceType()).isEqualTo(OracleVectorStoreDistanceType.EUCLIDEAN);
		assertThat(props.getIndexType()).isEqualTo(OracleVectorStoreIndexType.IVF);
		assertThat(props.isRemoveExistingVectorStoreTable()).isTrue();
	}

}
