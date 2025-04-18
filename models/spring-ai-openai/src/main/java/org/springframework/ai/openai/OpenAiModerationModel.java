package org.springframework.ai.openai;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.moderation.Categories;
import org.springframework.ai.moderation.CategoryScores;
import org.springframework.ai.moderation.Generation;
import org.springframework.ai.moderation.Moderation;
import org.springframework.ai.moderation.ModerationModel;
import org.springframework.ai.moderation.ModerationOptions;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResponse;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.ai.openai.api.OpenAiModerationApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

public class OpenAiModerationModel implements ModerationModel {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private final OpenAiModerationApi openAiModerationApi;

	private final RetryTemplate retryTemplate;

	private OpenAiModerationOptions defaultOptions;

	public OpenAiModerationModel(OpenAiModerationApi openAiModerationApi) {
		this(openAiModerationApi, RetryUtils.DEFAULT_RETRY_TEMPLATE);
	}

	public OpenAiModerationModel(OpenAiModerationApi openAiModerationApi, RetryTemplate retryTemplate) {
		Assert.notNull(openAiModerationApi, "OpenAiModerationApi must not be null");
		Assert.notNull(retryTemplate, "retryTemplate must not be null");
		this.openAiModerationApi = openAiModerationApi;
		this.retryTemplate = retryTemplate;
	}

	public OpenAiModerationOptions getDefaultOptions() {
		return this.defaultOptions;
	}

	public OpenAiModerationModel withDefaultOptions(OpenAiModerationOptions defaultOptions) {
		this.defaultOptions = defaultOptions;
		return this;
	}

	@Override
	public ModerationResponse call(ModerationPrompt moderationPrompt) {
		return this.retryTemplate.execute(ctx -> {

			String instructions = moderationPrompt.getInstructions().getText();

			OpenAiModerationApi.OpenAiModerationRequest moderationRequest = new OpenAiModerationApi.OpenAiModerationRequest(
					instructions);

			if (this.defaultOptions != null) {
				moderationRequest = ModelOptionsUtils.merge(this.defaultOptions, moderationRequest,
						OpenAiModerationApi.OpenAiModerationRequest.class);
			}

			if (moderationPrompt.getOptions() != null) {
				moderationRequest = ModelOptionsUtils.merge(toOpenAiModerationOptions(moderationPrompt.getOptions()),
						moderationRequest, OpenAiModerationApi.OpenAiModerationRequest.class);
			}

			ResponseEntity<OpenAiModerationApi.OpenAiModerationResponse> moderationResponseEntity = this.openAiModerationApi
				.createModeration(moderationRequest);

			return convertResponse(moderationResponseEntity, moderationRequest);
		});
	}

	private ModerationResponse convertResponse(
			ResponseEntity<OpenAiModerationApi.OpenAiModerationResponse> moderationResponseEntity,
			OpenAiModerationApi.OpenAiModerationRequest openAiModerationRequest) {
		OpenAiModerationApi.OpenAiModerationResponse moderationApiResponse = moderationResponseEntity.getBody();
		if (moderationApiResponse == null) {
			logger.warn("No moderation response returned for request: {}", openAiModerationRequest);
			return new ModerationResponse(new Generation());
		}

		List<ModerationResult> moderationResults = new ArrayList<>();
		if (moderationApiResponse.results() != null) {

			for (OpenAiModerationApi.OpenAiModerationResult result : moderationApiResponse.results()) {
				Categories categories = null;
				CategoryScores categoryScores = null;
				if (result.categories() != null) {
					categories = Categories.builder()
						.sexual(result.categories().sexual())
						.hate(result.categories().hate())
						.harassment(result.categories().harassment())
						.selfHarm(result.categories().selfHarm())
						.sexualMinors(result.categories().sexualMinors())
						.hateThreatening(result.categories().hateThreatening())
						.violenceGraphic(result.categories().violenceGraphic())
						.selfHarmIntent(result.categories().selfHarmIntent())
						.selfHarmInstructions(result.categories().selfHarmInstructions())
						.harassmentThreatening(result.categories().harassmentThreatening())
						.violence(result.categories().violence())
						.build();
				}
				if (result.categoryScores() != null) {
					categoryScores = CategoryScores.builder()
						.hate(result.categoryScores().hate())
						.hateThreatening(result.categoryScores().hateThreatening())
						.harassment(result.categoryScores().harassment())
						.harassmentThreatening(result.categoryScores().harassmentThreatening())
						.selfHarm(result.categoryScores().selfHarm())
						.selfHarmIntent(result.categoryScores().selfHarmIntent())
						.selfHarmInstructions(result.categoryScores().selfHarmInstructions())
						.sexual(result.categoryScores().sexual())
						.sexualMinors(result.categoryScores().sexualMinors())
						.violence(result.categoryScores().violence())
						.violenceGraphic(result.categoryScores().violenceGraphic())
						.build();
				}
				ModerationResult moderationResult = ModerationResult.builder()
					.categories(categories)
					.categoryScores(categoryScores)
					.flagged(result.flagged())
					.build();
				moderationResults.add(moderationResult);
			}

		}

		Moderation moderation = Moderation.builder()
			.id(moderationApiResponse.id())
			.model(moderationApiResponse.model())
			.results(moderationResults)
			.build();

		return new ModerationResponse(new Generation(moderation));
	}

	private OpenAiModerationOptions toOpenAiModerationOptions(ModerationOptions runtimeModerationOptions) {
		OpenAiModerationOptions.Builder openAiModerationOptionsBuilder = OpenAiModerationOptions.builder();

		if (runtimeModerationOptions != null && runtimeModerationOptions.getModel() != null) {
			openAiModerationOptionsBuilder.model(runtimeModerationOptions.getModel());
		}
		return openAiModerationOptionsBuilder.build();
	}

}
