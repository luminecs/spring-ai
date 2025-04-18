package org.springframework.ai.observation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiOperationMetadataTests {

	@Test
	void whenMandatoryMetadataThenReturn() {
		var operationMetadata = AiOperationMetadata.builder().operationType("chat").provider("doofenshmirtz").build();

		assertThat(operationMetadata).isNotNull();
	}

	@Test
	void whenOperationTypeIsNullThenThrow() {
		assertThatThrownBy(() -> AiOperationMetadata.builder().provider("doofenshmirtz").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("operationType cannot be null or empty");
	}

	@Test
	void whenOperationTypeIsEmptyThenThrow() {
		assertThatThrownBy(() -> AiOperationMetadata.builder().operationType("").provider("doofenshmirtz").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("operationType cannot be null or empty");
	}

	@Test
	void whenProviderIsNullThenThrow() {
		assertThatThrownBy(() -> AiOperationMetadata.builder().operationType("chat").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("provider cannot be null or empty");
	}

	@Test
	void whenProviderIsEmptyThenThrow() {
		assertThatThrownBy(() -> AiOperationMetadata.builder().operationType("chat").provider("").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("provider cannot be null or empty");
	}

}
