package org.springframework.ai.chat.metadata;

import java.time.Duration;

public interface RateLimit {

	Long getRequestsLimit();

	Long getRequestsRemaining();

	Duration getRequestsReset();

	Long getTokensLimit();

	Long getTokensRemaining();

	Duration getTokensReset();

}
