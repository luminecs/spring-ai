package org.springframework.ai.content;

import java.util.Map;

public interface Content {

	String getText();

	Map<String, Object> getMetadata();

}
