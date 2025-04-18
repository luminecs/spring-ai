package org.springframework.ai.reader;

import java.util.Map;

@FunctionalInterface
public interface JsonMetadataGenerator {

	Map<String, Object> generate(Map<String, Object> jsonMap);

}
