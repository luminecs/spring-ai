package org.springframework.ai.chat.client.advisor.api;

import reactor.core.publisher.Flux;

public interface StreamAroundAdvisor extends Advisor {

	Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain);

}
