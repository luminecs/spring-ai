package org.springframework.ai.stabilityai.api;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.stabilityai.StyleEnum;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StabilityAiImageOptions implements ImageOptions {

	@JsonProperty("samples")
	private Integer n;

	private String model = StabilityAiApi.DEFAULT_IMAGE_MODEL;

	@JsonProperty("width")
	private Integer width;

	@JsonProperty("height")
	private Integer height;

	private String responseFormat;

	@JsonProperty("cfg_scale")
	private Float cfgScale;

	@JsonProperty("clip_guidance_preset")
	private String clipGuidancePreset;

	@JsonProperty("sampler")
	private String sampler;

	@JsonProperty("seed")
	private Long seed;

	@JsonProperty("steps")
	private Integer steps;

	@JsonProperty("style_preset")
	private String stylePreset;

	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Integer getN() {
		return this.n;
	}

	public void setN(Integer n) {
		this.n = n;
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	public Integer getWidth() {
		return this.width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	@Override
	public Integer getHeight() {
		return this.height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	@Override
	public String getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(String responseFormat) {
		this.responseFormat = responseFormat;
	}

	public Float getCfgScale() {
		return this.cfgScale;
	}

	public void setCfgScale(Float cfgScale) {
		this.cfgScale = cfgScale;
	}

	public String getClipGuidancePreset() {
		return this.clipGuidancePreset;
	}

	public void setClipGuidancePreset(String clipGuidancePreset) {
		this.clipGuidancePreset = clipGuidancePreset;
	}

	public String getSampler() {
		return this.sampler;
	}

	public void setSampler(String sampler) {
		this.sampler = sampler;
	}

	public Long getSeed() {
		return this.seed;
	}

	public void setSeed(Long seed) {
		this.seed = seed;
	}

	public Integer getSteps() {
		return this.steps;
	}

	public void setSteps(Integer steps) {
		this.steps = steps;
	}

	@Override
	@JsonIgnore
	public String getStyle() {
		return getStylePreset();
	}

	@JsonIgnore
	public void setStyle(String style) {
		setStylePreset(style);
	}

	public String getStylePreset() {
		return this.stylePreset;
	}

	public void setStylePreset(String stylePreset) {
		this.stylePreset = stylePreset;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof StabilityAiImageOptions that)) {
			return false;
		}
		return Objects.equals(this.n, that.n) && Objects.equals(this.model, that.model)
				&& Objects.equals(this.width, that.width) && Objects.equals(this.height, that.height)
				&& Objects.equals(this.responseFormat, that.responseFormat)
				&& Objects.equals(this.cfgScale, that.cfgScale)
				&& Objects.equals(this.clipGuidancePreset, that.clipGuidancePreset)
				&& Objects.equals(this.sampler, that.sampler) && Objects.equals(this.seed, that.seed)
				&& Objects.equals(this.steps, that.steps) && Objects.equals(this.stylePreset, that.stylePreset);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.n, this.model, this.width, this.height, this.responseFormat, this.cfgScale,
				this.clipGuidancePreset, this.sampler, this.seed, this.steps, this.stylePreset);
	}

	@Override
	public String toString() {
		return "StabilityAiImageOptions{" + "n=" + this.n + ", model='" + this.model + '\'' + ", width=" + this.width
				+ ", height=" + this.height + ", responseFormat='" + this.responseFormat + '\'' + ", cfgScale="
				+ this.cfgScale + ", clipGuidancePreset='" + this.clipGuidancePreset + '\'' + ", sampler='"
				+ this.sampler + '\'' + ", seed=" + this.seed + ", steps=" + this.steps + ", stylePreset='"
				+ this.stylePreset + '\'' + '}';
	}

	public static final class Builder {

		private final StabilityAiImageOptions options;

		private Builder() {
			this.options = new StabilityAiImageOptions();
		}

		public Builder N(Integer n) {
			this.options.setN(n);
			return this;
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder width(Integer width) {
			this.options.setWidth(width);
			return this;
		}

		public Builder height(Integer height) {
			this.options.setHeight(height);
			return this;
		}

		public Builder responseFormat(String responseFormat) {
			this.options.setResponseFormat(responseFormat);
			return this;
		}

		public Builder cfgScale(Float cfgScale) {
			this.options.setCfgScale(cfgScale);
			return this;
		}

		public Builder clipGuidancePreset(String clipGuidancePreset) {
			this.options.setClipGuidancePreset(clipGuidancePreset);
			return this;
		}

		public Builder sampler(String sampler) {
			this.options.setSampler(sampler);
			return this;
		}

		public Builder seed(Long seed) {
			this.options.setSeed(seed);
			return this;
		}

		public Builder steps(Integer steps) {
			this.options.setSteps(steps);
			return this;
		}

		public Builder samples(Integer samples) {
			this.options.setN(samples);
			return this;
		}

		public Builder stylePreset(String stylePreset) {
			this.options.setStylePreset(stylePreset);
			return this;
		}

		public Builder stylePreset(StyleEnum styleEnum) {
			this.options.setStylePreset(styleEnum.toString());
			return this;
		}

		public StabilityAiImageOptions build() {
			return this.options;
		}

	}

}
