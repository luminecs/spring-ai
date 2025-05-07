package org.springframework.ai.mistralai;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import org.springframework.ai.mistralai.moderation.MistralAiModerationModel;
import org.springframework.ai.moderation.CategoryScores;
import org.springframework.ai.moderation.Moderation;
import org.springframework.ai.moderation.ModerationPrompt;
import org.springframework.ai.moderation.ModerationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = MistralAiTestConfiguration.class)
@EnabledIfEnvironmentVariable(named = "MISTRAL_AI_API_KEY", matches = ".+")
public class MistralAiModerationModelIT {

	@Autowired
	private MistralAiModerationModel mistralAiModerationModel;

	@Test
	void moderationAsPositiveTest() {
		var instructions = """
				I want to kill them.!".""";

		var moderationPrompt = new ModerationPrompt(instructions);

		var moderationResponse = this.mistralAiModerationModel.call(moderationPrompt);

		assertThat(moderationResponse.getResults()).hasSize(1);

		var generation = moderationResponse.getResult();
		Moderation moderation = generation.getOutput();
		assertThat(moderation.getId()).isNotEmpty();
		assertThat(moderation.getResults()).isNotNull();
		assertThat(moderation.getResults().size()).isNotZero();

		assertThat(moderation.getId()).isNotNull();
		assertThat(moderation.getModel()).isNotNull();

		ModerationResult result = moderation.getResults().get(0);
		assertThat(result.isFlagged()).isTrue();

		CategoryScores scores = result.getCategoryScores();
		assertThat(scores.getSexual()).isNotNull();
		assertThat(scores.getHate()).isNotNull();
		assertThat(scores.getViolence()).isNotNull();
		assertThat(scores.getDangerousAndCriminalContent()).isNotNull();
		assertThat(scores.getSelfHarm()).isNotNull();
		assertThat(scores.getHealth()).isNotNull();
		assertThat(scores.getFinancial()).isNotNull();
		assertThat(scores.getLaw()).isNotNull();
		assertThat(scores.getPii()).isNotNull();
	}

}
