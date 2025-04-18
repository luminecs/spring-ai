package org.springframework.ai.embedding;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.knuddels.jtokkit.api.EncodingType;

import org.springframework.ai.document.ContentFormatter;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator;
import org.springframework.ai.tokenizer.TokenCountEstimator;
import org.springframework.util.Assert;

public class TokenCountBatchingStrategy implements BatchingStrategy {

	private static final int MAX_INPUT_TOKEN_COUNT = 8191;

	private static final double DEFAULT_TOKEN_COUNT_RESERVE_PERCENTAGE = 0.1;

	private final TokenCountEstimator tokenCountEstimator;

	private final int maxInputTokenCount;

	private final ContentFormatter contentFormatter;

	private final MetadataMode metadataMode;

	public TokenCountBatchingStrategy() {
		this(EncodingType.CL100K_BASE, MAX_INPUT_TOKEN_COUNT, DEFAULT_TOKEN_COUNT_RESERVE_PERCENTAGE);
	}

	public TokenCountBatchingStrategy(EncodingType encodingType, int maxInputTokenCount, double reservePercentage) {
		this(encodingType, maxInputTokenCount, reservePercentage, Document.DEFAULT_CONTENT_FORMATTER,
				MetadataMode.NONE);
	}

	public TokenCountBatchingStrategy(EncodingType encodingType, int maxInputTokenCount, double reservePercentage,
			ContentFormatter contentFormatter, MetadataMode metadataMode) {
		Assert.notNull(encodingType, "EncodingType must not be null");
		Assert.isTrue(maxInputTokenCount > 0, "MaxInputTokenCount must be greater than 0");
		Assert.isTrue(reservePercentage >= 0 && reservePercentage < 1, "ReservePercentage must be in range [0, 1)");
		Assert.notNull(contentFormatter, "ContentFormatter must not be null");
		Assert.notNull(metadataMode, "MetadataMode must not be null");
		this.tokenCountEstimator = new JTokkitTokenCountEstimator(encodingType);
		this.maxInputTokenCount = (int) Math.round(maxInputTokenCount * (1 - reservePercentage));
		this.contentFormatter = contentFormatter;
		this.metadataMode = metadataMode;
	}

	public TokenCountBatchingStrategy(TokenCountEstimator tokenCountEstimator, int maxInputTokenCount,
			double reservePercentage, ContentFormatter contentFormatter, MetadataMode metadataMode) {
		Assert.notNull(tokenCountEstimator, "TokenCountEstimator must not be null");
		Assert.isTrue(maxInputTokenCount > 0, "MaxInputTokenCount must be greater than 0");
		Assert.isTrue(reservePercentage >= 0 && reservePercentage < 1, "ReservePercentage must be in range [0, 1)");
		Assert.notNull(contentFormatter, "ContentFormatter must not be null");
		Assert.notNull(metadataMode, "MetadataMode must not be null");
		this.tokenCountEstimator = tokenCountEstimator;
		this.maxInputTokenCount = (int) Math.round(maxInputTokenCount * (1 - reservePercentage));
		this.contentFormatter = contentFormatter;
		this.metadataMode = metadataMode;
	}

	@Override
	public List<List<Document>> batch(List<Document> documents) {
		List<List<Document>> batches = new ArrayList<>();
		int currentSize = 0;
		List<Document> currentBatch = new ArrayList<>();

		Map<Document, Integer> documentTokens = new LinkedHashMap<>();

		for (Document document : documents) {
			int tokenCount = this.tokenCountEstimator
				.estimate(document.getFormattedContent(this.contentFormatter, this.metadataMode));
			if (tokenCount > this.maxInputTokenCount) {
				throw new IllegalArgumentException(
						"Tokens in a single document exceeds the maximum number of allowed input tokens");
			}
			documentTokens.put(document, tokenCount);
		}

		for (Document document : documentTokens.keySet()) {
			Integer tokenCount = documentTokens.get(document);
			if (currentSize + tokenCount > this.maxInputTokenCount) {
				batches.add(currentBatch);
				currentBatch = new ArrayList<>();
				currentSize = 0;
			}
			currentBatch.add(document);
			currentSize += tokenCount;
		}
		if (!currentBatch.isEmpty()) {
			batches.add(currentBatch);
		}
		return batches;
	}

}
