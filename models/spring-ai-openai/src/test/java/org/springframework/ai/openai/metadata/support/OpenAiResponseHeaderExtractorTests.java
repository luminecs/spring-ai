package org.springframework.ai.openai.metadata.support;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import org.springframework.ai.openai.metadata.support.OpenAiResponseHeaderExtractor.DurationFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenAiResponseHeaderExtractorTests {

	@Test
	void parseTimeAsDurationWithDaysHoursMinutesSeconds() {

		Duration actual = DurationFormatter.TIME_UNIT.parse("6d18h22m45s");
		Duration expected = Duration.ofDays(6L)
			.plus(Duration.ofHours(18L))
			.plus(Duration.ofMinutes(22))
			.plus(Duration.ofSeconds(45L));

		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void parseTimeAsDurationWithMinutesSecondsMicrosecondsAndMicroseconds() {

		Duration actual = DurationFormatter.TIME_UNIT.parse("42m18s451ms21541ns");
		Duration expected = Duration.ofMinutes(42L)
			.plus(Duration.ofSeconds(18L))
			.plus(Duration.ofMillis(451))
			.plus(Duration.ofNanos(21541L));

		assertThat(actual).isEqualTo(expected);
	}

	@Test
	void parseTimeAsDurationWithDaysMinutesAndMilliseconds() {

		Duration actual = DurationFormatter.TIME_UNIT.parse("2d15m981ms");
		Duration expected = Duration.ofDays(2L).plus(Duration.ofMinutes(15L)).plus(Duration.ofMillis(981L));

		assertThat(actual).isEqualTo(expected);
	}

}
