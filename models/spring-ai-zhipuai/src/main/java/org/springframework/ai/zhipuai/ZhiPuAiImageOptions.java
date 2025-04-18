package org.springframework.ai.zhipuai;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.ai.image.ImageOptions;
import org.springframework.ai.zhipuai.api.ZhiPuAiImageApi;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZhiPuAiImageOptions implements ImageOptions {

	@JsonProperty("model")
	private String model = ZhiPuAiImageApi.DEFAULT_IMAGE_MODEL;

	@JsonProperty("user_id")
	private String user;

	public static Builder builder() {
		return new Builder();
	}

	@Override
	@JsonIgnore
	public Integer getN() {
		return null;
	}

	@Override
	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	@Override
	@JsonIgnore
	public Integer getWidth() {
		return null;
	}

	@Override
	@JsonIgnore
	public Integer getHeight() {
		return null;
	}

	@Override
	@JsonIgnore
	public String getResponseFormat() {
		return null;
	}

	@Override
	@JsonIgnore
	public String getStyle() {
		return null;
	}

	public String getUser() {
		return this.user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ZhiPuAiImageOptions that)) {
			return false;
		}
		return Objects.equals(this.model, that.model) && Objects.equals(this.user, that.user);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.model, this.user);
	}

	@Override
	public String toString() {
		return "ZhiPuAiImageOptions{model='" + this.model + '\'' + ", user='" + this.user + '\'' + '}';
	}

	public static final class Builder {

		private final ZhiPuAiImageOptions options;

		private Builder() {
			this.options = new ZhiPuAiImageOptions();
		}

		public Builder model(String model) {
			this.options.setModel(model);
			return this;
		}

		public Builder user(String user) {
			this.options.setUser(user);
			return this;
		}

		public ZhiPuAiImageOptions build() {
			return this.options;
		}

	}

}
