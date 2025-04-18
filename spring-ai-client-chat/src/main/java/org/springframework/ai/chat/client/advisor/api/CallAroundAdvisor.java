package org.springframework.ai.chat.client.advisor.api;

public interface CallAroundAdvisor extends Advisor {

	AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain);

}
