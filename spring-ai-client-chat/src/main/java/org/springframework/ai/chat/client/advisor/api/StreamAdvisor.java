package org.springframework.ai.chat.client.advisor.api;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import reactor.core.publisher.Flux;

public interface StreamAdvisor extends StreamAroundAdvisor {

	@Deprecated
	default Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		Flux<ChatClientResponse> chatClientResponse = adviseStream(advisedRequest.toChatClientRequest(), chain);
		return chatClientResponse.map(AdvisedResponse::from);
	}

	Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAroundAdvisorChain chain);

}
