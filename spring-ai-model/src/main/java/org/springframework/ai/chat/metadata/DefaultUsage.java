package org.springframework.ai.chat.metadata;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "promptTokens", "completionTokens", "totalTokens", "nativeUsage" })
public class DefaultUsage implements Usage {

	private final Integer promptTokens;

	private final Integer completionTokens;

	private final int totalTokens;

	private final Object nativeUsage;

	public DefaultUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens, Object nativeUsage) {
		this.promptTokens = promptTokens != null ? promptTokens : 0;
		this.completionTokens = completionTokens != null ? completionTokens : 0;
		this.totalTokens = totalTokens != null ? totalTokens
				: calculateTotalTokens(this.promptTokens, this.completionTokens);
		this.nativeUsage = nativeUsage;
	}

	public DefaultUsage(Integer promptTokens, Integer completionTokens) {
		this(promptTokens, completionTokens, null, null);
	}

	public DefaultUsage(Integer promptTokens, Integer completionTokens, Integer totalTokens) {
		this(promptTokens, completionTokens, totalTokens, null);
	}

	@JsonCreator
	public static DefaultUsage fromJson(@JsonProperty("promptTokens") Integer promptTokens,
			@JsonProperty("completionTokens") Integer completionTokens,
			@JsonProperty("totalTokens") Integer totalTokens, @JsonProperty("nativeUsage") Object nativeUsage) {
		return new DefaultUsage(promptTokens, completionTokens, totalTokens, nativeUsage);
	}

	@Override
	@JsonProperty("promptTokens")
	public Integer getPromptTokens() {
		return this.promptTokens;
	}

	@Override
	@JsonProperty("completionTokens")
	public Integer getCompletionTokens() {
		return this.completionTokens;
	}

	@Override
	@JsonProperty("totalTokens")
	public Integer getTotalTokens() {
		return this.totalTokens;
	}

	@Override
	@JsonProperty("nativeUsage")
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public Object getNativeUsage() {
		return this.nativeUsage;
	}

	private Integer calculateTotalTokens(Integer promptTokens, Integer completionTokens) {
		return promptTokens + completionTokens;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DefaultUsage that = (DefaultUsage) o;
		return this.totalTokens == that.totalTokens && Objects.equals(this.promptTokens, that.promptTokens)
				&& Objects.equals(this.completionTokens, that.completionTokens)
				&& Objects.equals(this.nativeUsage, that.nativeUsage);
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(this.promptTokens);
		result = 31 * result + Objects.hashCode(this.completionTokens);
		result = 31 * result + this.totalTokens;
		result = 31 * result + Objects.hashCode(this.nativeUsage);
		return result;
	}

	@Override
	public String toString() {
		return "DefaultUsage{" + "promptTokens=" + this.promptTokens + ", completionTokens=" + this.completionTokens
				+ ", totalTokens=" + this.totalTokens + '}';
	}

}
