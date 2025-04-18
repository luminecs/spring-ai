package org.springframework.ai.moderation;

import org.springframework.ai.model.Model;

@FunctionalInterface
public interface ModerationModel extends Model<ModerationPrompt, ModerationResponse> {

	ModerationResponse call(ModerationPrompt request);

}
