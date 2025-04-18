package org.springframework.ai.anthropic;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.anthropic.api.AnthropicApi.StreamEvent;
import org.springframework.core.io.DefaultResourceLoader;

import static org.assertj.core.api.Assertions.assertThat;

public class EventParsingTests {

	private static final Logger logger = LoggerFactory.getLogger(EventParsingTests.class);

	@Test
	public void readEvents() throws IOException {
		String json = new DefaultResourceLoader().getResource("classpath:/sample_events.json")
			.getContentAsString(Charset.defaultCharset());

		List<StreamEvent> events = new ObjectMapper().readerFor(new TypeReference<List<StreamEvent>>() {

		}).readValue(json);

		logger.info(events.toString());

		assertThat(events).hasSize(31);

	}

}
