package org.springframework.ai.embedding;

import java.util.Map;

import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.model.AbstractResponseMetadata;
import org.springframework.ai.model.ResponseMetadata;

public class EmbeddingResponseMetadata extends AbstractResponseMetadata implements ResponseMetadata {

	private String model;

	private Usage usage;

	public EmbeddingResponseMetadata() {
	}

	public EmbeddingResponseMetadata(String model, Usage usage) {
		this(model, usage, Map.of());
	}

	public EmbeddingResponseMetadata(String model, Usage usage, Map<String, Object> metadata) {
		this.model = model;
		this.usage = usage;
		for (Map.Entry<String, Object> entry : metadata.entrySet()) {
			this.map.put(entry.getKey(), entry.getValue());
		}
	}

	public String getModel() {
		return this.model != null ? this.model : "";
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Usage getUsage() {
		return this.usage != null ? this.usage : new EmptyUsage();
	}

	public void setUsage(Usage usage) {
		this.usage = usage;
	}

}
