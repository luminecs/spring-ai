package org.springframework.ai.vertexai.embedding.text;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.util.StringUtils;

@JsonInclude(Include.NON_NULL)
public class VertexAiTextEmbeddingOptions implements EmbeddingOptions {

	public static final String DEFAULT_MODEL_NAME = VertexAiTextEmbeddingModelName.TEXT_EMBEDDING_004.getName();

	private @JsonProperty("model") String model;

	// @formatter:off

	private @JsonProperty("task") TaskType taskType;

	private @JsonProperty("dimensions") Integer dimensions;

	private @JsonProperty("title") String title;

	private @JsonProperty("autoTruncate") Boolean autoTruncate;

	public static Builder builder() {
		return new Builder();
	}

	// @formatter:on

	public VertexAiTextEmbeddingOptions initializeDefaults() {

		if (this.getTaskType() == null) {
			this.setTaskType(TaskType.RETRIEVAL_DOCUMENT);
		}

		if (StringUtils.hasText(this.getTitle()) && this.getTaskType() != TaskType.RETRIEVAL_DOCUMENT) {
			throw new IllegalArgumentException("Title is only valid with task_type=RETRIEVAL_DOCUMENT");
		}

		return this;
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public TaskType getTaskType() {
		return this.taskType;
	}

	public void setTaskType(TaskType taskType) {
		this.taskType = taskType;
	}

	@Override
	public Integer getDimensions() {
		return this.dimensions;
	}

	public void setDimensions(Integer dimensions) {
		this.dimensions = dimensions;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(String user) {
		this.title = user;
	}

	public Boolean getAutoTruncate() {
		return this.autoTruncate;
	}

	public void setAutoTruncate(Boolean autoTruncate) {
		this.autoTruncate = autoTruncate;
	}

	public enum TaskType {

		RETRIEVAL_QUERY,

		RETRIEVAL_DOCUMENT,

		SEMANTIC_SIMILARITY,

		CLASSIFICATION,

		CLUSTERING,

		QUESTION_ANSWERING,

		FACT_VERIFICATION

	}

	public static class Builder {

		protected VertexAiTextEmbeddingOptions options;

		public Builder() {
			this.options = new VertexAiTextEmbeddingOptions();
		}

		public Builder from(VertexAiTextEmbeddingOptions fromOptions) {
			if (fromOptions.getDimensions() != null) {
				this.options.setDimensions(fromOptions.getDimensions());
			}
			if (StringUtils.hasText(fromOptions.getModel())) {
				this.options.setModel(fromOptions.getModel());
			}
			if (fromOptions.getTaskType() != null) {
				this.options.setTaskType(fromOptions.getTaskType());
			}
			if (StringUtils.hasText(fromOptions.getTitle())) {
				this.options.setTitle(fromOptions.getTitle());
			}
			return this;
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder model(VertexAiTextEmbeddingModelName model) {
			this.options.setModel(model.getName());
			return this;
		}

		public Builder taskType(TaskType taskType) {
			this.options.setTaskType(taskType);
			return this;
		}

		public Builder dimensions(Integer dimensions) {
			this.options.dimensions = dimensions;
			return this;
		}

		public Builder title(String user) {
			this.options.setTitle(user);
			return this;
		}

		public Builder autoTruncate(Boolean autoTruncate) {
			this.options.setAutoTruncate(autoTruncate);
			return this;
		}

		public VertexAiTextEmbeddingOptions build() {
			return this.options;
		}

	}

}
