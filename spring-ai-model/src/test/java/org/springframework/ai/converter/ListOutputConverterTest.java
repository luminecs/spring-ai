package org.springframework.ai.converter;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.core.convert.support.DefaultConversionService;

import static org.assertj.core.api.Assertions.assertThat;

class ListOutputConverterTest {

	@Test
	void csv() {
		String csvAsString = "foo, bar, baz";
		ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());
		List<String> list = listOutputConverter.convert(csvAsString);
		assertThat(list).containsExactlyElementsOf(List.of("foo", "bar", "baz"));
	}

}
