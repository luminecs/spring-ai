package org.springframework.ai.model;

import java.util.List;

public interface ModelResponse<T extends ModelResult<?>> {

	T getResult();

	List<T> getResults();

	ResponseMetadata getMetadata();

}
