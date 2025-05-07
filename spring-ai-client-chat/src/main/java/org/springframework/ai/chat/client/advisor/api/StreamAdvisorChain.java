package org.springframework.ai.chat.client.advisor.api;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import reactor.core.publisher.Flux;

import java.util.List;

public interface StreamAdvisorChain extends AdvisorChain {

	Flux<ChatClientResponse> nextStream(ChatClientRequest chatClientRequest);

	List<StreamAdvisor> getStreamAdvisors();

}
