package org.springframework.ai.chat.metadata;

import java.time.Duration;

public class EmptyRateLimit implements RateLimit {

	@Override
	public Long getRequestsLimit() {
		return 0L;
	}

	@Override
	public Long getRequestsRemaining() {
		return 0L;
	}

	@Override
	public Duration getRequestsReset() {
		return Duration.ZERO;
	}

	@Override
	public Long getTokensLimit() {
		return 0L;
	}

	@Override
	public Long getTokensRemaining() {
		return 0L;
	}

	@Override
	public Duration getTokensReset() {
		return Duration.ZERO;
	}

}
