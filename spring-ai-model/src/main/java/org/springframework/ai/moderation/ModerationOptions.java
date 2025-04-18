package org.springframework.ai.moderation;

import org.springframework.ai.model.ModelOptions;

public interface ModerationOptions extends ModelOptions {

	String getModel();

}
