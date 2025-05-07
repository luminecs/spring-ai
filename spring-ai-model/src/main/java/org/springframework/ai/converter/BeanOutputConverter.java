package org.springframework.ai.converter;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.util.JacksonUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.NonNull;

import static org.springframework.ai.util.LoggingMarkers.SENSITIVE_DATA_MARKER;

public class BeanOutputConverter<T> implements StructuredOutputConverter<T> {

	private final Logger logger = LoggerFactory.getLogger(BeanOutputConverter.class);

	private final Type type;

	private final ObjectMapper objectMapper;

	private String jsonSchema;

	public BeanOutputConverter(Class<T> clazz) {
		this(ParameterizedTypeReference.forType(clazz));
	}

	public BeanOutputConverter(Class<T> clazz, ObjectMapper objectMapper) {
		this(ParameterizedTypeReference.forType(clazz), objectMapper);
	}

	public BeanOutputConverter(ParameterizedTypeReference<T> typeRef) {
		this(typeRef.getType(), null);
	}

	public BeanOutputConverter(ParameterizedTypeReference<T> typeRef, ObjectMapper objectMapper) {
		this(typeRef.getType(), objectMapper);
	}

	private BeanOutputConverter(Type type, ObjectMapper objectMapper) {
		Objects.requireNonNull(type, "Type cannot be null;");
		this.type = type;
		this.objectMapper = objectMapper != null ? objectMapper : getObjectMapper();
		generateSchema();
	}

	private void generateSchema() {
		JacksonModule jacksonModule = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED,
				JacksonOption.RESPECT_JSONPROPERTY_ORDER);
		SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
				com.github.victools.jsonschema.generator.SchemaVersion.DRAFT_2020_12,
				com.github.victools.jsonschema.generator.OptionPreset.PLAIN_JSON)
			.with(jacksonModule)
			.with(Option.FORBIDDEN_ADDITIONAL_PROPERTIES_BY_DEFAULT);
		SchemaGeneratorConfig config = configBuilder.build();
		SchemaGenerator generator = new SchemaGenerator(config);
		JsonNode jsonNode = generator.generateSchema(this.type);
		ObjectWriter objectWriter = this.objectMapper.writer(new DefaultPrettyPrinter()
			.withObjectIndenter(new DefaultIndenter().withLinefeed(System.lineSeparator())));
		try {
			this.jsonSchema = objectWriter.writeValueAsString(jsonNode);
		}
		catch (JsonProcessingException e) {
			logger.error("Could not pretty print json schema for jsonNode: {}", jsonNode);
			throw new RuntimeException("Could not pretty print json schema for " + this.type, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public T convert(@NonNull String text) {
		try {

			text = text.trim();

			if (text.startsWith("```") && text.endsWith("```")) {

				String[] lines = text.split("\n", 2);
				if (lines[0].trim().equalsIgnoreCase("```json")) {
					text = lines.length > 1 ? lines[1] : "";
				}
				else {
					text = text.substring(3);
				}

				text = text.substring(0, text.length() - 3);

				text = text.trim();
			}
			return (T) this.objectMapper.readValue(text, this.objectMapper.constructType(this.type));
		}
		catch (JsonProcessingException e) {
			logger.error(SENSITIVE_DATA_MARKER,
					"Could not parse the given text to the desired target type: \"{}\" into {}", text, this.type);
			throw new RuntimeException(e);
		}
	}

	protected ObjectMapper getObjectMapper() {
		return JsonMapper.builder()
			.addModules(JacksonUtils.instantiateAvailableModules())
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.build();
	}

	@Override
	public String getFormat() {
		String template = """
				Your response should be in JSON format.
				Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
				Do not include markdown code blocks in your response.
				Remove the ```json markdown from the output.
				Here is the JSON Schema instance your output must adhere to:
				```%s```
				""";
		return String.format(template, this.jsonSchema);
	}

	public String getJsonSchema() {
		return this.jsonSchema;
	}

	public Map<String, Object> getJsonSchemaMap() {
		try {
			return this.objectMapper.readValue(this.jsonSchema, Map.class);
		}
		catch (JsonProcessingException ex) {
			logger.error("Could not parse the JSON Schema to a Map object", ex);
			throw new IllegalStateException(ex);
		}
	}

}
