package org.springframework.ai.chat.client.advisor.api;

import org.springframework.ai.chat.client.ChatClientRequest;

@Deprecated
public interface CallAroundAdvisorChain {

	@Deprecated
	AdvisedResponse nextAroundCall(AdvisedRequest advisedRequest);

}
