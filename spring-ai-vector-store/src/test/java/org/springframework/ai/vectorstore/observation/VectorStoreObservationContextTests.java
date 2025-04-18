package org.springframework.ai.vectorstore.observation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectorStoreObservationContextTests {

	@Test
	void whenMandatoryFieldsThenReturn() {
		var observationContext = VectorStoreObservationContext
			.builder("db", VectorStoreObservationContext.Operation.ADD)
			.build();
		assertThat(observationContext).isNotNull();
	}

	@Test
	void whenDbSystemIsNullThenThrow() {
		assertThatThrownBy(() -> VectorStoreObservationContext.builder(null, "delete").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("databaseSystem cannot be null or empty");
	}

	@Test
	void whenOperationNameIsNullThenThrow() {
		assertThatThrownBy(() -> VectorStoreObservationContext.builder("Db", "").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("operationName cannot be null or empty");
	}

}
