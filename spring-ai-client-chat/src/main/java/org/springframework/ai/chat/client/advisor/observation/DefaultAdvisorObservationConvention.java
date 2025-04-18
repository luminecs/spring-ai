package org.springframework.ai.chat.client.advisor.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;

import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationDocumentation.HighCardinalityKeyNames;
import org.springframework.ai.chat.client.advisor.observation.AdvisorObservationDocumentation.LowCardinalityKeyNames;
import org.springframework.ai.observation.conventions.AiOperationType;
import org.springframework.ai.observation.conventions.AiProvider;
import org.springframework.ai.observation.conventions.SpringAiKind;
import org.springframework.ai.util.ParsingUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class DefaultAdvisorObservationConvention implements AdvisorObservationConvention {

	public static final String DEFAULT_NAME = "spring.ai.advisor";

	private final String name;

	public DefaultAdvisorObservationConvention() {
		this(DEFAULT_NAME);
	}

	public DefaultAdvisorObservationConvention(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	@Nullable
	public String getContextualName(AdvisorObservationContext context) {
		Assert.notNull(context, "context cannot be null");
		return ParsingUtils.reConcatenateCamelCase(context.getAdvisorName(), "_")
			.replace("_around_advisor", "")
			.replace("_advisor", "");
	}

	@Override
	public KeyValues getLowCardinalityKeyValues(AdvisorObservationContext context) {
		Assert.notNull(context, "context cannot be null");
		return KeyValues.of(aiOperationType(context), aiProvider(context), springAiKind(), advisorType(context),
				advisorName(context));
	}

	protected KeyValue aiOperationType(AdvisorObservationContext context) {
		return KeyValue.of(LowCardinalityKeyNames.AI_OPERATION_TYPE, AiOperationType.FRAMEWORK.value());
	}

	protected KeyValue aiProvider(AdvisorObservationContext context) {
		return KeyValue.of(LowCardinalityKeyNames.AI_PROVIDER, AiProvider.SPRING_AI.value());
	}

	@Deprecated
	protected KeyValue advisorType(AdvisorObservationContext context) {
		return KeyValue.of(LowCardinalityKeyNames.ADVISOR_TYPE, context.getAdvisorType().name());
	}

	protected KeyValue springAiKind() {
		return KeyValue.of(LowCardinalityKeyNames.SPRING_AI_KIND, SpringAiKind.ADVISOR.value());
	}

	protected KeyValue advisorName(AdvisorObservationContext context) {
		return KeyValue.of(LowCardinalityKeyNames.ADVISOR_NAME, context.getAdvisorName());
	}

	@Override
	public KeyValues getHighCardinalityKeyValues(AdvisorObservationContext context) {
		Assert.notNull(context, "context cannot be null");
		return KeyValues.of(advisorOrder(context));
	}

	protected KeyValue advisorOrder(AdvisorObservationContext context) {
		return KeyValue.of(HighCardinalityKeyNames.ADVISOR_ORDER, "" + context.getOrder());
	}

}
