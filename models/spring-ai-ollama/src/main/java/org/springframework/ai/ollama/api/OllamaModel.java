package org.springframework.ai.ollama.api;

import org.springframework.ai.model.ChatModelDescription;

public enum OllamaModel implements ChatModelDescription {

	QWEN_2_5_7B("qwen2.5"),

	QWQ("qwq"),

	LLAMA2("llama2"),

	LLAMA3("llama3"),

	LLAMA3_1("llama3.1"),

	LLAMA3_2("llama3.2"),

	LLAMA3_2_VISION_11b("llama3.2-vision"),

	LLAMA3_2_VISION_90b("llama3.2-vision:90b"),

	LLAMA3_2_1B("llama3.2:1b"),

	LLAMA3_2_3B("llama3.2:3b"),

	MISTRAL("mistral"),

	MISTRAL_NEMO("mistral-nemo"),

	MOONDREAM("moondream"),

	DOLPHIN_PHI("dolphin-phi"),

	PHI("phi"),

	PHI3("phi3"),

	NEURAL_CHAT("neural-chat"),

	STARLING_LM("starling-lm"),

	CODELLAMA("codellama"),

	ORCA_MINI("orca-mini"),

	LLAVA("llava"),

	GEMMA("gemma"),

	LLAMA2_UNCENSORED("llama2-uncensored"),

	NOMIC_EMBED_TEXT("nomic-embed-text"),

	MXBAI_EMBED_LARGE("mxbai-embed-large");

	private final String id;

	OllamaModel(String id) {
		this.id = id;
	}

	public String id() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.id;
	}

}
