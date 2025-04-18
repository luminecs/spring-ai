package org.springframework.ai.util.json.schema;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.victools.jsonschema.generator.FieldScope;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigPart;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

public final class SpringAiSchemaModule implements Module {

	private final boolean requiredByDefault;

	public SpringAiSchemaModule(Option... options) {
		this.requiredByDefault = Stream.of(options)
			.noneMatch(option -> option == Option.PROPERTY_REQUIRED_FALSE_BY_DEFAULT);
	}

	@Override
	public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
		this.applyToConfigBuilder(builder.forFields());
	}

	private void applyToConfigBuilder(SchemaGeneratorConfigPart<FieldScope> configPart) {
		configPart.withDescriptionResolver(this::resolveDescription);
		configPart.withRequiredCheck(this::checkRequired);
	}

	@Nullable
	private String resolveDescription(MemberScope<?, ?> member) {
		var toolParamAnnotation = member.getAnnotationConsideringFieldAndGetter(ToolParam.class);
		if (toolParamAnnotation != null && StringUtils.hasText(toolParamAnnotation.description())) {
			return toolParamAnnotation.description();
		}
		return null;
	}

	private boolean checkRequired(MemberScope<?, ?> member) {
		var toolParamAnnotation = member.getAnnotationConsideringFieldAndGetter(ToolParam.class);
		if (toolParamAnnotation != null) {
			return toolParamAnnotation.required();
		}

		var propertyAnnotation = member.getAnnotationConsideringFieldAndGetter(JsonProperty.class);
		if (propertyAnnotation != null) {
			return propertyAnnotation.required();
		}

		var schemaAnnotation = member.getAnnotationConsideringFieldAndGetter(Schema.class);
		if (schemaAnnotation != null) {
			return schemaAnnotation.requiredMode() == Schema.RequiredMode.REQUIRED
					|| schemaAnnotation.requiredMode() == Schema.RequiredMode.AUTO || schemaAnnotation.required();
		}

		var nullableAnnotation = member.getAnnotationConsideringFieldAndGetter(Nullable.class);
		if (nullableAnnotation != null) {
			return false;
		}

		return this.requiredByDefault;
	}

	public enum Option {

		PROPERTY_REQUIRED_FALSE_BY_DEFAULT

	}

}
