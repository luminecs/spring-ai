package org.springframework.ai.chat.client.advisor.api;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import reactor.core.publisher.Flux;

public interface StreamAdvisorChain extends StreamAroundAdvisorChain {

	@Deprecated
	default Flux<AdvisedResponse> nextAroundStream(AdvisedRequest advisedRequest) {
		Flux<ChatClientResponse> chatClientResponse = nextStream(advisedRequest.toChatClientRequest());
		return chatClientResponse.map(AdvisedResponse::from);
	}

	Flux<ChatClientResponse> nextStream(ChatClientRequest chatClientRequest);

}
