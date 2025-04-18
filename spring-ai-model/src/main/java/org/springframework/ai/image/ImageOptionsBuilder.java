package org.springframework.ai.image;

public final class ImageOptionsBuilder {

	private final DefaultImageModelOptions options = new DefaultImageModelOptions();

	private ImageOptionsBuilder() {

	}

	public static ImageOptionsBuilder builder() {
		return new ImageOptionsBuilder();
	}

	public ImageOptionsBuilder N(Integer n) {
		this.options.setN(n);
		return this;
	}

	public ImageOptionsBuilder model(String model) {
		this.options.setModel(model);
		return this;
	}

	public ImageOptionsBuilder responseFormat(String responseFormat) {
		this.options.setResponseFormat(responseFormat);
		return this;
	}

	public ImageOptionsBuilder width(Integer width) {
		this.options.setWidth(width);
		return this;
	}

	public ImageOptionsBuilder height(Integer height) {
		this.options.setHeight(height);
		return this;
	}

	public ImageOptionsBuilder style(String style) {
		this.options.setStyle(style);
		return this;
	}

	public ImageOptions build() {
		return this.options;
	}

	private static class DefaultImageModelOptions implements ImageOptions {

		private Integer n;

		private String model;

		private Integer width;

		private Integer height;

		private String responseFormat;

		private String style;

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
		public String getResponseFormat() {
			return this.responseFormat;
		}

		public void setResponseFormat(String responseFormat) {
			this.responseFormat = responseFormat;
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
		public String getStyle() {
			return this.style;
		}

		public void setStyle(String style) {
			this.style = style;
		}

	}

}
