package org.springframework.ai.chat.client.advisor.api;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;

public interface CallAdvisorChain extends CallAroundAdvisorChain {

	@Deprecated
	default AdvisedResponse nextAroundCall(AdvisedRequest advisedRequest) {
		ChatClientResponse chatClientResponse = nextCall(advisedRequest.toChatClientRequest());
		return AdvisedResponse.from(chatClientResponse);
	}

	ChatClientResponse nextCall(ChatClientRequest chatClientRequest);

}
