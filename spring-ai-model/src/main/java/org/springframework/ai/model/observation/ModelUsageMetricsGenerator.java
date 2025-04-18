package org.springframework.ai.model.observation;

import java.util.ArrayList;
import java.util.List;

import io.micrometer.common.KeyValue;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.observation.Observation;

import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.observation.conventions.AiObservationMetricAttributes;
import org.springframework.ai.observation.conventions.AiObservationMetricNames;
import org.springframework.ai.observation.conventions.AiTokenType;

public final class ModelUsageMetricsGenerator {

	private static final String DESCRIPTION = "Measures number of input and output tokens used";

	private ModelUsageMetricsGenerator() {
	}

	public static void generate(Usage usage, Observation.Context context, MeterRegistry meterRegistry) {

		if (usage.getPromptTokens() != null) {
			Counter.builder(AiObservationMetricNames.TOKEN_USAGE.value())
				.tag(AiObservationMetricAttributes.TOKEN_TYPE.value(), AiTokenType.INPUT.value())
				.description(DESCRIPTION)
				.tags(createTags(context))
				.register(meterRegistry)
				.increment(usage.getPromptTokens());
		}

		if (usage.getCompletionTokens() != null) {
			Counter.builder(AiObservationMetricNames.TOKEN_USAGE.value())
				.tag(AiObservationMetricAttributes.TOKEN_TYPE.value(), AiTokenType.OUTPUT.value())
				.description(DESCRIPTION)
				.tags(createTags(context))
				.register(meterRegistry)
				.increment(usage.getCompletionTokens());
		}

		if (usage.getTotalTokens() != null) {
			Counter.builder(AiObservationMetricNames.TOKEN_USAGE.value())
				.tag(AiObservationMetricAttributes.TOKEN_TYPE.value(), AiTokenType.TOTAL.value())
				.description(DESCRIPTION)
				.tags(createTags(context))
				.register(meterRegistry)
				.increment(usage.getTotalTokens());
		}

	}

	private static List<Tag> createTags(Observation.Context context) {
		List<Tag> tags = new ArrayList<>();
		for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
			tags.add(Tag.of(keyValue.getKey(), keyValue.getValue()));
		}
		return tags;
	}

}
