package org.springframework.ai.chat.client.advisor.observation;

import java.util.Map;

import io.micrometer.observation.Observation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class AdvisorObservationContext extends Observation.Context {

	private final String advisorName;

	private final Type advisorType;

	private final int order;

	@Nullable
	private AdvisedRequest advisorRequest;

	@Nullable
	private Map<String, Object> advisorRequestContext;

	@Nullable
	private Map<String, Object> advisorResponseContext;

	public AdvisorObservationContext(String advisorName, Type advisorType, @Nullable AdvisedRequest advisorRequest,
			@Nullable Map<String, Object> advisorRequestContext, @Nullable Map<String, Object> advisorResponseContext,
			int order) {
		Assert.hasText(advisorName, "advisorName must not be null or empty");
		Assert.notNull(advisorType, "advisorType must not be null");

		this.advisorName = advisorName;
		this.advisorType = advisorType;
		this.advisorRequest = advisorRequest;
		this.advisorRequestContext = advisorRequestContext;
		this.advisorResponseContext = advisorResponseContext;
		this.order = order;
	}

	public static Builder builder() {
		return new Builder();
	}

	public String getAdvisorName() {
		return this.advisorName;
	}

	public Type getAdvisorType() {
		return this.advisorType;
	}

	@Nullable
	public AdvisedRequest getAdvisedRequest() {
		return this.advisorRequest;
	}

	public void setAdvisedRequest(@Nullable AdvisedRequest advisedRequest) {
		this.advisorRequest = advisedRequest;
	}

	@Nullable
	public Map<String, Object> getAdvisorRequestContext() {
		return this.advisorRequestContext;
	}

	public void setAdvisorRequestContext(@Nullable Map<String, Object> advisorRequestContext) {
		this.advisorRequestContext = advisorRequestContext;
	}

	@Nullable
	public Map<String, Object> getAdvisorResponseContext() {
		return this.advisorResponseContext;
	}

	public void setAdvisorResponseContext(@Nullable Map<String, Object> advisorResponseContext) {
		this.advisorResponseContext = advisorResponseContext;
	}

	public int getOrder() {
		return this.order;
	}

	public enum Type {

		BEFORE,

		AFTER,

		AROUND

	}

	public static final class Builder {

		private String advisorName;

		private Type advisorType;

		private AdvisedRequest advisorRequest;

		private Map<String, Object> advisorRequestContext;

		private Map<String, Object> advisorResponseContext;

		private int order = 0;

		private Builder() {
		}

		public Builder advisorName(String advisorName) {
			this.advisorName = advisorName;
			return this;
		}

		public Builder advisorType(Type advisorType) {
			this.advisorType = advisorType;
			return this;
		}

		public Builder advisedRequest(AdvisedRequest advisedRequest) {
			this.advisorRequest = advisedRequest;
			return this;
		}

		public Builder advisorRequestContext(Map<String, Object> advisorRequestContext) {
			this.advisorRequestContext = advisorRequestContext;
			return this;
		}

		public Builder advisorResponseContext(Map<String, Object> advisorResponseContext) {
			this.advisorResponseContext = advisorResponseContext;
			return this;
		}

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public AdvisorObservationContext build() {
			return new AdvisorObservationContext(this.advisorName, this.advisorType, this.advisorRequest,
					this.advisorRequestContext, this.advisorResponseContext, this.order);
		}

	}

}
