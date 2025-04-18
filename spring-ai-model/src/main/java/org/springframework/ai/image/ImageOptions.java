package org.springframework.ai.image;

import org.springframework.ai.model.ModelOptions;
import org.springframework.lang.Nullable;

public interface ImageOptions extends ModelOptions {

	@Nullable
	Integer getN();

	@Nullable
	String getModel();

	@Nullable
	Integer getWidth();

	@Nullable
	Integer getHeight();

	@Nullable
	String getResponseFormat();

	@Nullable
	String getStyle();

}
