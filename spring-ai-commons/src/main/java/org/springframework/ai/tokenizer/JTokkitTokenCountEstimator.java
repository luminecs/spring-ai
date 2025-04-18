package org.springframework.ai.tokenizer;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;

import org.springframework.ai.content.Media;
import org.springframework.ai.content.MediaContent;
import org.springframework.util.CollectionUtils;

public class JTokkitTokenCountEstimator implements TokenCountEstimator {

	private final Encoding estimator;

	public JTokkitTokenCountEstimator() {
		this(EncodingType.CL100K_BASE);
	}

	public JTokkitTokenCountEstimator(EncodingType tokenEncodingType) {
		this.estimator = Encodings.newLazyEncodingRegistry().getEncoding(tokenEncodingType);
	}

	@Override
	public int estimate(String text) {
		if (text == null) {
			return 0;
		}
		return this.estimator.countTokens(text);
	}

	@Override
	public int estimate(MediaContent content) {
		int tokenCount = 0;

		if (content.getText() != null) {
			tokenCount += this.estimate(content.getText());
		}

		if (!CollectionUtils.isEmpty(content.getMedia())) {

			for (Media media : content.getMedia()) {

				tokenCount += this.estimate(media.getMimeType().toString());

				if (media.getData() instanceof String textData) {
					tokenCount += this.estimate(textData);
				}
				else if (media.getData() instanceof byte[] binaryData) {
					tokenCount += binaryData.length;
				}
			}
		}

		return tokenCount;
	}

	@Override
	public int estimate(Iterable<MediaContent> contents) {
		int totalSize = 0;
		for (MediaContent mediaContent : contents) {
			totalSize += this.estimate(mediaContent);
		}
		return totalSize;
	}

}
