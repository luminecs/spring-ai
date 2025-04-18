package org.springframework.ai.chat.client.advisor.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import org.springframework.util.Assert;

public interface BaseAdvisor extends CallAroundAdvisor, StreamAroundAdvisor {

	Scheduler DEFAULT_SCHEDULER = Schedulers.boundedElastic();

	@Override
	default AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
		Assert.notNull(advisedRequest, "advisedRequest cannot be null");
		Assert.notNull(chain, "chain cannot be null");

		AdvisedRequest processedAdvisedRequest = before(advisedRequest);
		AdvisedResponse advisedResponse = chain.nextAroundCall(processedAdvisedRequest);
		return after(advisedResponse);
	}

	@Override
	default Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
		Assert.notNull(advisedRequest, "advisedRequest cannot be null");
		Assert.notNull(chain, "chain cannot be null");
		Assert.notNull(getScheduler(), "scheduler cannot be null");

		Flux<AdvisedResponse> advisedResponses = Mono.just(advisedRequest)
			.publishOn(getScheduler())
			.map(this::before)
			.flatMapMany(chain::nextAroundStream);

		return advisedResponses.map(ar -> {
			if (AdvisedResponseStreamUtils.onFinishReason().test(ar)) {
				ar = after(ar);
			}
			return ar;
		}).onErrorResume(error -> Flux.error(new IllegalStateException("Stream processing failed", error)));
	}

	@Override
	default String getName() {
		return this.getClass().getSimpleName();
	}

	AdvisedRequest before(AdvisedRequest request);

	AdvisedResponse after(AdvisedResponse advisedResponse);

	default Scheduler getScheduler() {
		return DEFAULT_SCHEDULER;
	}

}
