package org.springframework.ai.ollama.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.jupiter.api.Test;

import org.springframework.ai.model.ModelOptionsUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class OllamaDurationFieldsTests {

	@Test
	public void testDurationFields() throws JsonMappingException, JsonProcessingException {

		var value = ModelOptionsUtils.jsonToObject("""
				{
					"model": "llama3.2",
					"created_at": "2023-08-04T19:22:45.499127Z",
					"response": "",
					"done": true,
					"total_duration": 10706818083,
					"load_duration": 6338219291,
					"prompt_eval_count": 26,
					"prompt_eval_duration": 130079000,
					"eval_count": 259,
					"eval_duration": 4232710000
				}
				""", OllamaApi.ChatResponse.class);

		assertThat(value.getTotalDuration().toNanos()).isEqualTo(10706818083L);
		assertThat(value.getLoadDuration().toNanos()).isEqualTo(6338219291L);
		assertThat(value.getEvalDuration().toNanos()).isEqualTo(4232710000L);
		assertThat(value.getPromptEvalDuration().toNanos()).isEqualTo(130079000L);
	}

}
