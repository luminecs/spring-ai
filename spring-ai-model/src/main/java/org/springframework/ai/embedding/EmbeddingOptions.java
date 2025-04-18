package org.springframework.ai.embedding;

import org.springframework.ai.model.ModelOptions;
import org.springframework.lang.Nullable;

public interface EmbeddingOptions extends ModelOptions {

	@Nullable
	String getModel();

	@Nullable
	Integer getDimensions();

}
