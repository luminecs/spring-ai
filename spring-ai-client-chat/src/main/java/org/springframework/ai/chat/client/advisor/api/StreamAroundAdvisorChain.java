package org.springframework.ai.chat.client.advisor.api;

import org.springframework.ai.chat.client.ChatClientRequest;
import reactor.core.publisher.Flux;

@Deprecated
public interface StreamAroundAdvisorChain {

	@Deprecated
	Flux<AdvisedResponse> nextAroundStream(AdvisedRequest advisedRequest);

}
