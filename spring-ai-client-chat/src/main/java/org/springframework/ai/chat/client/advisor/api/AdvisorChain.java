package org.springframework.ai.chat.client.advisor.api;

import io.micrometer.observation.ObservationRegistry;

public interface AdvisorChain {

	default ObservationRegistry getObservationRegistry() {
		return ObservationRegistry.NOOP;
	}

}
