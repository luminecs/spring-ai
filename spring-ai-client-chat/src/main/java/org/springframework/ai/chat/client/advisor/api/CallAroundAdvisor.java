package org.springframework.ai.chat.client.advisor.api;

import org.springframework.ai.chat.client.ChatClientRequest;

@Deprecated
public interface CallAroundAdvisor extends Advisor {

	@Deprecated
	AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain);

}
