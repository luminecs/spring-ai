package org.springframework.ai.model.observation;

import io.micrometer.observation.Observation;

import org.springframework.ai.observation.AiOperationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class ModelObservationContext<REQ, RES> extends Observation.Context {

	private final REQ request;

	private final AiOperationMetadata operationMetadata;

	@Nullable
	private RES response;

	public ModelObservationContext(REQ request, AiOperationMetadata operationMetadata) {
		Assert.notNull(request, "request cannot be null");
		Assert.notNull(operationMetadata, "operationMetadata cannot be null");
		this.request = request;
		this.operationMetadata = operationMetadata;
	}

	public REQ getRequest() {
		return this.request;
	}

	public AiOperationMetadata getOperationMetadata() {
		return this.operationMetadata;
	}

	@Nullable
	public RES getResponse() {
		return this.response;
	}

	public void setResponse(RES response) {
		Assert.notNull(response, "response cannot be null");
		this.response = response;
	}

}
