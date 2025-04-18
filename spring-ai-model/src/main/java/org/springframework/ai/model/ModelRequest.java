package org.springframework.ai.model;

public interface ModelRequest<T> {

	T getInstructions();

	ModelOptions getOptions();

}
