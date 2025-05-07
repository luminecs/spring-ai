package org.springframework.ai.chat.client.advisor.api;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;

import java.util.List;

public interface CallAdvisorChain extends AdvisorChain {

	ChatClientResponse nextCall(ChatClientRequest chatClientRequest);

	List<CallAdvisor> getCallAdvisors();

}
