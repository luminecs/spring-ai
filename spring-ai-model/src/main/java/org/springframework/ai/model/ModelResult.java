package org.springframework.ai.model;

public interface ModelResult<T> {

	T getOutput();

	ResultMetadata getMetadata();

}
