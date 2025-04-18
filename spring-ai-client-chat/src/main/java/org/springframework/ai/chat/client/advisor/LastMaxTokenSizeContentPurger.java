package org.springframework.ai.chat.client.advisor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.content.Content;
import org.springframework.ai.content.MediaContent;
import org.springframework.ai.tokenizer.TokenCountEstimator;

public class LastMaxTokenSizeContentPurger {

	protected final TokenCountEstimator tokenCountEstimator;

	protected final int maxTokenSize;

	public LastMaxTokenSizeContentPurger(TokenCountEstimator tokenCountEstimator, int maxTokenSize) {
		this.tokenCountEstimator = tokenCountEstimator;
		this.maxTokenSize = maxTokenSize;
	}

	public List<Content> purgeExcess(List<MediaContent> datum, int totalSize) {

		int index = 0;
		List<Content> newList = new ArrayList<>();

		while (index < datum.size() && totalSize > this.maxTokenSize) {
			MediaContent oldDatum = datum.get(index++);
			int oldMessageTokenSize = this.doEstimateTokenCount(oldDatum);
			totalSize = totalSize - oldMessageTokenSize;
		}

		if (index >= datum.size()) {
			return List.of();
		}

		newList.addAll(datum.subList(index, datum.size()));

		return newList;
	}

	protected int doEstimateTokenCount(MediaContent datum) {
		return this.tokenCountEstimator.estimate(datum);
	}

	protected int doEstimateTokenCount(List<MediaContent> datum) {
		return datum.stream().mapToInt(this::doEstimateTokenCount).sum();
	}

}
