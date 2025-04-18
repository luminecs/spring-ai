package org.springframework.ai.openai;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.image.ImageOptions;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiImageOptions implements ImageOptions {

	@JsonProperty("n")
	private Integer n;

	@JsonProperty("model")
	private String model;

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

	public String getQuality() {
		return this.quality;
	}

	public void setQuality(String quality) {
		this.quality = quality;
	}

	@Override
	public String getResponseFormat() {
		return this.responseFormat;
	}

	public void setResponseFormat(String responseFormat) {
		this.responseFormat = responseFormat;
	}

	@Override
	public Integer getWidth() {
		if (this.width != null) {
			return this.width;
		}
		else if (this.size != null) {
			try {
				String[] dimensions = this.size.split("x");
				if (dimensions.length != 2) {
					return null;
				}
				return Integer.parseInt(dimensions[0]);
			}
			catch (Exception ex) {
				return null;
			}
		}
		return null;
	}

	public void setWidth(Integer width) {
		this.width = width;
		if (this.width != null && this.height != null) {
			this.size = this.width + "x" + this.height;
		}
	}

	@Override
	public Integer getHeight() {
		if (this.height != null) {
			return this.height;
		}
		else if (this.size != null) {
			try {
				String[] dimensions = this.size.split("x");
				if (dimensions.length != 2) {
					return null;
				}
				return Integer.parseInt(dimensions[1]);
			}
			catch (Exception ex) {
				return null;
			}
		}
		return null;
	}

	public void setHeight(Integer height) {
		this.height = height;
		if (this.width != null && this.height != null) {
			this.size = this.width + "x" + this.height;
		}
	}

	@Override
	public String getStyle() {
		return this.style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof OpenAiImageOptions that)) {
			return false;
		}
		return Objects.equals(this.n, that.n) && Objects.equals(this.model, that.model)
				&& Objects.equals(this.width, that.width) && Objects.equals(this.height, that.height)
				&& Objects.equals(this.quality, that.quality)
				&& Objects.equals(this.responseFormat, that.responseFormat) && Objects.equals(this.size, that.size)
				&& Objects.equals(this.style, that.style) && Objects.equals(this.user, that.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.n, this.model, this.width, this.height, this.quality, this.responseFormat, this.size,
				this.style, this.user);
	}

	@Override
	public String toString() {
		return "OpenAiImageOptions{" + "n=" + this.n + ", model='" + this.model + '\'' + ", width=" + this.width
				+ ", height=" + this.height + ", quality='" + this.quality + '\'' + ", responseFormat='"
				+ this.responseFormat + '\'' + ", size='" + this.size + '\'' + ", style='" + this.style + '\''
				+ ", user='" + this.user + '\'' + '}';
	}

	public static final class Builder {

		private final OpenAiImageOptions options;

		private Builder() {
			this.options = new OpenAiImageOptions();
		}

		public Builder N(Integer n) {
			this.options.setN(n);
			return this;
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder quality(String quality) {
			this.options.setQuality(quality);
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

		public Builder style(String style) {
			this.options.setStyle(style);
			return this;
		}

		public Builder user(String user) {
			this.options.setUser(user);
			return this;
		}

		public Builder withN(Integer n) {
			this.options.setN(n);
			return this;
		}

		public Builder withModel(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder withQuality(String quality) {
			this.options.setQuality(quality);
			return this;
		}

		public Builder withResponseFormat(String responseFormat) {
			this.options.setResponseFormat(responseFormat);
			return this;
		}

		public Builder withWidth(Integer width) {
			this.options.setWidth(width);
			return this;
		}

		public Builder withHeight(Integer height) {
			this.options.setHeight(height);
			return this;
		}

		public Builder withStyle(String style) {
			this.options.setStyle(style);
			return this;
		}

		public Builder withUser(String user) {
			this.options.setUser(user);
			return this;
		}

		public OpenAiImageOptions build() {
			return this.options;
		}

	}

}
