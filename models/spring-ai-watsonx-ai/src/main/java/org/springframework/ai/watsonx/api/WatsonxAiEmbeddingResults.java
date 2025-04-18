package org.springframework.ai.watsonx.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WatsonxAiEmbeddingResults(@JsonProperty("embedding") float[] embedding) {

}
