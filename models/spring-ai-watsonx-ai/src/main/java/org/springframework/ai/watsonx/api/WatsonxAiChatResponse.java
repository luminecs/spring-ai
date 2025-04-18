package org.springframework.ai.watsonx.api;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// @formatter:off
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WatsonxAiChatResponse(
		@JsonProperty("model_id") String modelId,
		@JsonProperty("created_at") Date createdAt,
		@JsonProperty("results") List<WatsonxAiChatResults> results,
		@JsonProperty("system") Map<String, Object> system
) { }
