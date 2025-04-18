package org.springframework.ai.azure.openai;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.image.ImageOptions;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AzureOpenAiImageOptions implements ImageOptions {

	public static final String DEFAULT_IMAGE_MODEL = ImageModel.DALL_E_3.getValue();

	@JsonProperty("n")
	private Integer n;

	@JsonProperty("model")
	private String model = ImageModel.DALL_E_3.value;

	@JsonProperty("deployment_name")
	private String deploymentName;

	@JsonProperty("size_width")
	private Integer width;

	@JsonProperty("size_height")
	private Integer height;

	@JsonProperty("quality")
	private String quality;

	@JsonProperty("response_format")
	private String responseFormat;

	@JsonProperty("size")
	private String size;

	@JsonProperty("style")
	private String style;

	@JsonProperty("user")
	private String user;

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
		this.size = this.width + "x" + this.height;
	}

	@Override
	public Integer getHeight() {
		return this.height;
	}

	public void setHeight(Integer height) {
		this.height = height;
		this.size = this.width + "x" + this.height;
	}

	@Override
	public String getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(String responseFormat) {
		this.responseFormat = responseFormat;
	}

	public String getSize() {
		if (this.size != null) {
			return this.size;
		}
		return (this.width != null && this.height != null) ? this.width + "x" + this.height : null;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getQuality() {
		return this.quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	@Override
	public String getStyle() {
		return this.style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getDeploymentName() {
		return this.deploymentName;
	}

	public void setDeploymentName(String deploymentName) {
		this.deploymentName = deploymentName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof AzureOpenAiImageOptions that)) {
			return false;
		}
		return Objects.equals(this.n, that.n) && Objects.equals(this.model, that.model)
				&& Objects.equals(this.deploymentName, that.deploymentName) && Objects.equals(this.width, that.width)
				&& Objects.equals(this.height, that.height) && Objects.equals(this.quality, that.quality)
				&& Objects.equals(this.responseFormat, that.responseFormat) && Objects.equals(this.size, that.size)
				&& Objects.equals(this.style, that.style) && Objects.equals(this.user, that.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.n, this.model, this.deploymentName, this.width, this.height, this.quality,
				this.responseFormat, this.size, this.style, this.user);
	}

	@Override
	public String toString() {
		return "AzureOpenAiImageOptions{" + "n=" + this.n + ", model='" + this.model + '\'' + ", deploymentName='"
				+ this.deploymentName + '\'' + ", width=" + this.width + ", height=" + this.height + ", quality='"
				+ this.quality + '\'' + ", responseFormat='" + this.responseFormat + '\'' + ", size='" + this.size
				+ '\'' + ", style='" + this.style + '\'' + ", user='" + this.user + '\'' + '}';
	}

	public enum ImageModel {

		DALL_E_3("dall-e-3"),

		DALL_E_2("dall-e-2");

		private final String value;

		ImageModel(String model) {
			this.value = model;
		}

		public String getValue() {
			return this.value;
		}

	}

	public static final class Builder {

		private final AzureOpenAiImageOptions options;

		private Builder() {
			this.options = new AzureOpenAiImageOptions();
		}

		public Builder N(Integer n) {
			this.options.setN(n);
			return this;
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder deploymentName(String deploymentName) {
			this.options.setDeploymentName(deploymentName);
			return this;
		}

		public Builder responseFormat(String responseFormat) {
			this.options.setResponseFormat(responseFormat);
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

		public Builder user(String user) {
			this.options.setUser(user);
			return this;
		}

		public Builder style(String style) {
			this.options.setStyle(style);
			return this;
		}

		public AzureOpenAiImageOptions build() {
			return this.options;
		}

	}

}
