package org.springframework.ai.vectorstore.observation;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.util.CollectionUtils;

public final class VectorStoreObservationContentProcessor {

	private VectorStoreObservationContentProcessor() {
	}

	public static List<String> documents(VectorStoreObservationContext context) {
		if (CollectionUtils.isEmpty(context.getQueryResponse())) {
			return List.of();
		}

		return context.getQueryResponse().stream().map(Document::getText).toList();
	}

}
