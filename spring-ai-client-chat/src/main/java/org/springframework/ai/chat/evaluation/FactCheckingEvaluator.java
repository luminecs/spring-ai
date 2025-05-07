package org.springframework.ai.chat.evaluation;

import java.util.Collections;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.evaluation.Evaluator;

public class FactCheckingEvaluator implements Evaluator {

	private static final String DEFAULT_EVALUATION_PROMPT_TEXT = """
				Evaluate whether or not the following claim is supported by the provided document.
				Respond with "yes" if the claim is supported, or "no" if it is not.
				Document: \\n {document}\\n
				Claim: \\n {claim}
			""";

	private static final String BESPOKE_EVALUATION_PROMPT_TEXT = """
				Document: \\n {document}\\n
				Claim: \\n {claim}
			""";

	private final ChatClient.Builder chatClientBuilder;

	private final String evaluationPrompt;

	public FactCheckingEvaluator(ChatClient.Builder chatClientBuilder) {
		this(chatClientBuilder, DEFAULT_EVALUATION_PROMPT_TEXT);
	}

	public FactCheckingEvaluator(ChatClient.Builder chatClientBuilder, String evaluationPrompt) {
		this.chatClientBuilder = chatClientBuilder;
		this.evaluationPrompt = evaluationPrompt;
	}

	public static FactCheckingEvaluator forBespokeMinicheck(ChatClient.Builder chatClientBuilder) {
		return new FactCheckingEvaluator(chatClientBuilder, BESPOKE_EVALUATION_PROMPT_TEXT);
	}

	@Override
	public EvaluationResponse evaluate(EvaluationRequest evaluationRequest) {
		var response = evaluationRequest.getResponseContent();
		var context = doGetSupportingData(evaluationRequest);

		String evaluationResponse = this.chatClientBuilder.build()
			.prompt()
			.user(userSpec -> userSpec.text(this.evaluationPrompt).param("document", context).param("claim", response))
			.call()
			.content();

		boolean passing = evaluationResponse.equalsIgnoreCase("yes");
		return new EvaluationResponse(passing, "", Collections.emptyMap());
	}

}
