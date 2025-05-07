package org.springframework.ai.chat.client.advisor.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationDocumentation.HighCardinalityKeyNames;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationDocumentation.LowCardinalityKeyNames;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.observation.conventions.AiOperationType;
import org.springframework.ai.observation.conventions.AiProvider;
import org.springframework.ai.observation.conventions.SpringAiKind;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultAdvisorObservationConventionTests {

	private final DefaultAdvisorObservationConvention observationConvention = new DefaultAdvisorObservationConvention();

	@Test
	void shouldHaveName() {
		assertThat(this.observationConvention.getName()).isEqualTo(DefaultAdvisorObservationConvention.DEFAULT_NAME);
	}

	@Test
	void contextualName() {
		AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
			.chatClientRequest(ChatClientRequest.builder().prompt(new Prompt("Hello")).build())
			.advisorName("MyName")
			.build();
		assertThat(this.observationConvention.getContextualName(observationContext)).isEqualTo("my_name");
	}

	@Test
	void supportsAdvisorObservationContext() {
		AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
			.chatClientRequest(ChatClientRequest.builder().prompt(new Prompt("Hello")).build())
			.advisorName("MyName")
			.build();
		assertThat(this.observationConvention.supportsContext(observationContext)).isTrue();
		assertThat(this.observationConvention.supportsContext(new Observation.Context())).isFalse();
	}

	@Test
	void shouldHaveLowCardinalityKeyValuesWhenDefined() {
		AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
			.chatClientRequest(ChatClientRequest.builder().prompt(new Prompt("Hello")).build())
			.advisorName("MyName")
			.build();
		assertThat(this.observationConvention.getLowCardinalityKeyValues(observationContext)).contains(
				KeyValue.of(LowCardinalityKeyNames.AI_OPERATION_TYPE.asString(), AiOperationType.FRAMEWORK.value()),
				KeyValue.of(LowCardinalityKeyNames.AI_PROVIDER.asString(), AiProvider.SPRING_AI.value()),
				KeyValue.of(LowCardinalityKeyNames.ADVISOR_NAME.asString(), "MyName"),
				KeyValue.of(LowCardinalityKeyNames.SPRING_AI_KIND.asString(), SpringAiKind.ADVISOR.value()));
	}

	@Test
	void shouldHaveKeyValuesWhenDefinedAndResponse() {
		AdvisorObservationContext observationContext = AdvisorObservationContext.builder()
			.chatClientRequest(ChatClientRequest.builder().prompt(new Prompt("Hello")).build())
			.advisorName("MyName")
			.order(678)
			.build();

		assertThat(this.observationConvention.getHighCardinalityKeyValues(observationContext))
			.contains(KeyValue.of(HighCardinalityKeyNames.ADVISOR_ORDER.asString(), "678"));
	}

}
