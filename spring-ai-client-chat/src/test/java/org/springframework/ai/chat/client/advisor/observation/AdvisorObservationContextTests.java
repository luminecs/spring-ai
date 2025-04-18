package org.springframework.ai.chat.client.advisor.observation;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AdvisorObservationContextTests {

	@Test
	void whenMandatoryOptionsThenReturn() {
		AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
			.advisorName("MyName")
			.advisorType(AdvisorObservationContext.Type.BEFORE)
			.build();

		assertThat(observationContext).isNotNull();
	}

	@Test
	void missingAdvisorName() {
		assertThatThrownBy(
				() -> AdvisorObservationContext.builder().advisorType(AdvisorObservationContext.Type.BEFORE).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("advisorName must not be null or empty");
	}

	@Test
	void missingAdvisorType() {
		assertThatThrownBy(() -> AdvisorObservationContext.builder().advisorName("MyName").build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("advisorType must not be null");
	}

}
