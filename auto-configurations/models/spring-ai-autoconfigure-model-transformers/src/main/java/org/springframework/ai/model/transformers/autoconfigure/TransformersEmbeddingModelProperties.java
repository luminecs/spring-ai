package org.springframework.ai.model.transformers.autoconfigure;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(TransformersEmbeddingModelProperties.CONFIG_PREFIX)
public class TransformersEmbeddingModelProperties {

	public static final String CONFIG_PREFIX = "spring.ai.embedding.transformer";

	public static final String DEFAULT_CACHE_DIRECTORY = new File(System.getProperty("java.io.tmpdir"),
			"spring-ai-onnx-generative")
		.getAbsolutePath();

	@NestedConfigurationProperty
	private final Tokenizer tokenizer = new Tokenizer();

	@NestedConfigurationProperty
	private final Cache cache = new Cache();

	@NestedConfigurationProperty
	private final Onnx onnx = new Onnx();

	private MetadataMode metadataMode = MetadataMode.NONE;

	public Cache getCache() {
		return this.cache;
	}

	public Onnx getOnnx() {
		return this.onnx;
	}

	public Tokenizer getTokenizer() {
		return this.tokenizer;
	}

	public MetadataMode getMetadataMode() {
		return this.metadataMode;
	}

	public void setMetadataMode(MetadataMode metadataMode) {
		this.metadataMode = metadataMode;
	}

	public static class Tokenizer {

		private String uri = TransformersEmbeddingModel.DEFAULT_ONNX_TOKENIZER_URI;

		@NestedConfigurationProperty
		private Map<String, String> options = new HashMap<>();

		public String getUri() {
			return this.uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public Map<String, String> getOptions() {
			return this.options;
		}

		public void setOptions(Map<String, String> options) {
			this.options = options;
		}

	}

	public static class Cache {

		private boolean enabled = true;

		private String directory = DEFAULT_CACHE_DIRECTORY;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public String getDirectory() {
			return this.directory;
		}

		public void setDirectory(String directory) {
			this.directory = directory;
		}

	}

	public static class Onnx {

		private String modelUri = TransformersEmbeddingModel.DEFAULT_ONNX_MODEL_URI;

		private String modelOutputName = TransformersEmbeddingModel.DEFAULT_MODEL_OUTPUT_NAME;

		private int gpuDeviceId = -1;

		public String getModelUri() {
			return this.modelUri;
		}

		public void setModelUri(String modelUri) {
			this.modelUri = modelUri;
		}

		public int getGpuDeviceId() {
			return this.gpuDeviceId;
		}

		public void setGpuDeviceId(int gpuDeviceId) {
			this.gpuDeviceId = gpuDeviceId;
		}

		public String getModelOutputName() {
			return this.modelOutputName;
		}

		public void setModelOutputName(String modelOutputName) {
			this.modelOutputName = modelOutputName;
		}

	}

}
