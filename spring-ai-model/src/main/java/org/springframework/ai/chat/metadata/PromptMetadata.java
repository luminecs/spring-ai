package org.springframework.ai.chat.metadata;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.springframework.util.Assert;

@FunctionalInterface
public interface PromptMetadata extends Iterable<PromptMetadata.PromptFilterMetadata> {

	static PromptMetadata empty() {
		return of();
	}

	static PromptMetadata of(PromptFilterMetadata... array) {
		return of(Arrays.asList(array));
	}

	static PromptMetadata of(Iterable<PromptFilterMetadata> iterable) {
		Assert.notNull(iterable, "An Iterable of PromptFilterMetadata must not be null");
		return iterable::iterator;
	}

	default Optional<PromptFilterMetadata> findByPromptIndex(int promptIndex) {

		Assert.isTrue(promptIndex > -1, "Prompt index [%d] must be greater than equal to 0".formatted(promptIndex));

		return StreamSupport.stream(this.spliterator(), false)
			.filter(promptFilterMetadata -> promptFilterMetadata.getPromptIndex() == promptIndex)
			.findFirst();
	}

	interface PromptFilterMetadata {

		static PromptFilterMetadata from(int promptIndex, Object contentFilterMetadata) {

			return new PromptFilterMetadata() {

				@Override
				public int getPromptIndex() {
					return promptIndex;
				}

				@Override
				@SuppressWarnings("unchecked")
				public <T> T getContentFilterMetadata() {
					return (T) contentFilterMetadata;
				}
			};
		}

		int getPromptIndex();

		<T> T getContentFilterMetadata();

	}

}
