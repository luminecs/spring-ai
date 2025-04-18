package org.springframework.ai.converter;

import org.springframework.core.convert.support.DefaultConversionService;

public abstract class AbstractConversionServiceOutputConverter<T> implements StructuredOutputConverter<T> {

	private final DefaultConversionService conversionService;

	public AbstractConversionServiceOutputConverter(DefaultConversionService conversionService) {
		this.conversionService = conversionService;
	}

	public DefaultConversionService getConversionService() {
		return this.conversionService;
	}

}
