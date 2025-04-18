package org.springframework.ai.watsonx.api;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WatsonxAiEmbeddingResponse(@JsonProperty("model_id") String model,
		@JsonProperty("created_at") Date createdAt, @JsonProperty("results") List<WatsonxAiEmbeddingResults> results,
		@JsonProperty("input_token_count") Integer inputTokenCount) {

}
