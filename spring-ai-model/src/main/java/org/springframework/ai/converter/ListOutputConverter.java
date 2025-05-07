package org.springframework.ai.converter;

import java.util.List;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.lang.NonNull;

public class ListOutputConverter extends AbstractConversionServiceOutputConverter<List<String>> {

	public ListOutputConverter() {
		this(new DefaultConversionService());
	}

	public ListOutputConverter(DefaultConversionService defaultConversionService) {
		super(defaultConversionService);
	}

	@Override
	public String getFormat() {
		return """
				Respond with only a list of comma-separated values, without any leading or trailing text.
				Example format: foo, bar, baz
				""";
	}

	@Override
	public List<String> convert(@NonNull String text) {
		return this.getConversionService().convert(text, List.class);
	}

}
