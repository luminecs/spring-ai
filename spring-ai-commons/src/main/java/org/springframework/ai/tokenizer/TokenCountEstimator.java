package org.springframework.ai.tokenizer;

import org.springframework.ai.content.MediaContent;

public interface TokenCountEstimator {

	int estimate(String text);

	int estimate(MediaContent content);

	int estimate(Iterable<MediaContent> messages);

}
