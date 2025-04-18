package org.springframework.ai.chat.client.advisor.api;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;

public interface CallAdvisor extends CallAroundAdvisor {

	@Deprecated
	default AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		ChatClientResponse chatClientResponse = adviseCall(advisedRequest.toChatClientRequest(), chain);
		return AdvisedResponse.from(chatClientResponse);
	}

	ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAroundAdvisorChain chain);

}
