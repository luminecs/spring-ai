package org.springframework.ai.azure.openai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.util.StringUtils;

@JsonInclude(Include.NON_NULL)
public class AzureOpenAiResponseFormat {

	@JsonProperty("type")
	private Type type;

	@JsonProperty("json_schema")
	private JsonSchema jsonSchema = null;

	private String schema;

	public AzureOpenAiResponseFormat() {

	}

	public Type getType() {
		return this.type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public JsonSchema getJsonSchema() {
		return this.jsonSchema;
	}

	public void setJsonSchema(JsonSchema jsonSchema) {
		this.jsonSchema = jsonSchema;
	}

	public String getSchema() {
		return this.schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
		if (schema != null) {
			this.jsonSchema = JsonSchema.builder().schema(schema).strict(true).build();
		}
	}

	private AzureOpenAiResponseFormat(Type type, JsonSchema jsonSchema) {
		this.type = type;
		this.jsonSchema = jsonSchema;
	}

	public AzureOpenAiResponseFormat(Type type, String schema) {
		this(type, StringUtils.hasText(schema) ? JsonSchema.builder().schema(schema).strict(true).build() : null);
	}

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AzureOpenAiResponseFormat that = (AzureOpenAiResponseFormat) o;
		return this.type == that.type && Objects.equals(this.jsonSchema, that.jsonSchema);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.type, this.jsonSchema);
	}

	@Override
	public String toString() {
		return "ResponseFormat{" + "type=" + this.type + ", jsonSchema=" + this.jsonSchema + '}';
	}

	public static final class Builder {

		private Type type;

		private JsonSchema jsonSchema;

		private Builder() {
		}

		public Builder type(Type type) {
			this.type = type;
			return this;
		}

		public Builder jsonSchema(JsonSchema jsonSchema) {
			this.jsonSchema = jsonSchema;
			return this;
		}

		public Builder jsonSchema(String jsonSchema) {
			this.jsonSchema = JsonSchema.builder().schema(jsonSchema).build();
			return this;
		}

		public AzureOpenAiResponseFormat build() {
			return new AzureOpenAiResponseFormat(this.type, this.jsonSchema);
		}

	}

	public enum Type {

		@JsonProperty("text")
		TEXT,

		@JsonProperty("json_object")
		JSON_OBJECT,

		@JsonProperty("json_schema")
		JSON_SCHEMA

	}

	@JsonInclude(Include.NON_NULL)
	public static class JsonSchema {

		@JsonProperty("name")
		private String name;

		@JsonProperty("schema")
		private Map<String, Object> schema;

		@JsonProperty("strict")
		private Boolean strict;

		public JsonSchema() {

		}

		public String getName() {
			return this.name;
		}

		public Map<String, Object> getSchema() {
			return this.schema;
		}

		public Boolean getStrict() {
			return this.strict;
		}

		private JsonSchema(String name, Map<String, Object> schema, Boolean strict) {
			this.name = name;
			this.schema = schema;
			this.strict = strict;
		}

		public static Builder builder() {
			return new Builder();
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.name, this.schema, this.strict);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			JsonSchema that = (JsonSchema) o;
			return Objects.equals(this.name, that.name) && Objects.equals(this.schema, that.schema)
					&& Objects.equals(this.strict, that.strict);
		}

		public static final class Builder {

			private String name = "custom_schema";

			private Map<String, Object> schema;

			private Boolean strict = true;

			private Builder() {
			}

			public Builder name(String name) {
				this.name = name;
				return this;
			}

			public Builder schema(Map<String, Object> schema) {
				this.schema = schema;
				return this;
			}

			public Builder schema(String schema) {
				this.schema = ModelOptionsUtils.jsonToMap(schema);
				return this;
			}

			public Builder strict(Boolean strict) {
				this.strict = strict;
				return this;
			}

			public JsonSchema build() {
				return new JsonSchema(this.name, this.schema, this.strict);
			}

		}

	}

}
