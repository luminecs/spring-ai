package org.springframework.ai.chat.client.advisor.api;

import org.springframework.core.Ordered;

public interface Advisor extends Ordered {

	int DEFAULT_CHAT_MEMORY_PRECEDENCE_ORDER = Ordered.HIGHEST_PRECEDENCE + 1000;

	String getName();

}
