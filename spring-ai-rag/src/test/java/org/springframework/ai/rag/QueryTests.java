package org.springframework.ai.rag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryTests {

	@Test
	void whenTextIsNullThenThrow() {
		assertThatThrownBy(() -> new Query(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("text cannot be null or empty");
	}

	@Test
	void whenTextIsEmptyThenThrow() {
		assertThatThrownBy(() -> new Query("")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("text cannot be null or empty");
	}

}
