package org.springframework.ai.converter;

import org.springframework.core.convert.converter.Converter;

public interface StructuredOutputConverter<T> extends Converter<String, T>, FormatProvider {

}
