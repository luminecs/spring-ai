package org.springframework.ai.evaluation;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.util.StringUtils;

@FunctionalInterface
public interface Evaluator {

	EvaluationResponse evaluate(EvaluationRequest evaluationRequest);

	default String doGetSupportingData(EvaluationRequest evaluationRequest) {
		List<Document> data = evaluationRequest.getDataList();
		return data.stream()
			.map(Document::getText)
			.filter(StringUtils::hasText)
			.collect(Collectors.joining(System.lineSeparator()));
	}

}
