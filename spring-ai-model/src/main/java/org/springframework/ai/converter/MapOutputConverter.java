package org.springframework.ai.converter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.support.MessageBuilder;

public class MapOutputConverter extends AbstractMessageOutputConverter<Map<String, Object>> {

	public MapOutputConverter() {
		super(new MappingJackson2MessageConverter());
	}

	@Override
	public Map<String, Object> convert(@NonNull String text) {
		if (text.startsWith("```json") && text.endsWith("```")) {
			text = text.substring(7, text.length() - 3);
		}

		Message<?> message = MessageBuilder.withPayload(text.getBytes(StandardCharsets.UTF_8)).build();
		return (Map) this.getMessageConverter().fromMessage(message, HashMap.class);
	}

	@Override
	public String getFormat() {
		String raw = """
				Your response should be in JSON format.
				The data structure for the JSON should match this Java class: %s
				Do not include any explanations, only provide a RFC8259 compliant JSON response following this format without deviation.
				Remove the ```json markdown surrounding the output including the trailing "```".
				""";
		return String.format(raw, HashMap.class.getName());
	}

}
