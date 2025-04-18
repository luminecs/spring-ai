package org.springframework.ai.chat.client.advisor.api;

import org.springframework.ai.chat.client.ChatClientRequest;
import reactor.core.publisher.Flux;

@Deprecated
public interface StreamAroundAdvisor extends Advisor {

	@Deprecated
	Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain);

}
