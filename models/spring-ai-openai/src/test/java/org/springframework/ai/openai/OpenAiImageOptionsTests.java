package org.springframework.ai.openai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiImageOptionsTests {

	@Test
	void whenImageDimensionsAreAllUnset() {
		OpenAiImageOptions options = new OpenAiImageOptions();
		assertThat(options.getHeight()).isEqualTo(null);
		assertThat(options.getWidth()).isEqualTo(null);
		assertThat(options.getSize()).isEqualTo(null);
	}

	@Test
	void whenSizeIsSet() {
		OpenAiImageOptions options = new OpenAiImageOptions();
		options.setSize("1920x1080");
		assertThat(options.getHeight()).isEqualTo(1080);
		assertThat(options.getWidth()).isEqualTo(1920);
		assertThat(options.getSize()).isEqualTo("1920x1080");
	}

	@Test
	void whenWidthAndHeightAreSet() {
		OpenAiImageOptions options = new OpenAiImageOptions();
		options.setWidth(1920);
		options.setHeight(1080);
		assertThat(options.getHeight()).isEqualTo(1080);
		assertThat(options.getWidth()).isEqualTo(1920);
		assertThat(options.getSize()).isEqualTo("1920x1080");
	}

	@Test
	void whenWidthIsSet() {
		OpenAiImageOptions options = new OpenAiImageOptions();
		options.setWidth(1920);
		assertThat(options.getHeight()).isNull();
		assertThat(options.getWidth()).isEqualTo(1920);

		assertThat(options.getSize()).isNull();
	}

	@Test
	void whenHeightIsSet() {
		OpenAiImageOptions options = new OpenAiImageOptions();
		options.setHeight(1080);
		assertThat(options.getHeight()).isEqualTo(1080);
		assertThat(options.getWidth()).isNull();

		assertThat(options.getSize()).isNull();
	}

	@Test
	void whenInvalidSizeFormatIsSet() {
		OpenAiImageOptions options = new OpenAiImageOptions();
		options.setSize("invalid");
		assertThat(options.getHeight()).isNull();
		assertThat(options.getWidth()).isNull();
		assertThat(options.getSize()).isEqualTo("invalid");
	}

	@Test
	void whenSizeWithInvalidNumbersIsSet() {
		OpenAiImageOptions options = new OpenAiImageOptions();
		options.setSize("axb");
		assertThat(options.getHeight()).isNull();
		assertThat(options.getWidth()).isNull();
		assertThat(options.getSize()).isEqualTo("axb");
	}

	@Test
	void whenSizeWithMissingDimensionIsSet() {
		OpenAiImageOptions options = new OpenAiImageOptions();
		options.setSize("1024x");
		assertThat(options.getHeight()).isNull();
		assertThat(options.getWidth()).isNull();
		assertThat(options.getSize()).isEqualTo("1024x");
	}

	@Test
	void whenSizeWithExtraSeparatorsIsSet() {
		OpenAiImageOptions options = new OpenAiImageOptions();
		options.setSize("1024x1024x1024");
		assertThat(options.getHeight()).isNull();
		assertThat(options.getWidth()).isNull();
		assertThat(options.getSize()).isEqualTo("1024x1024x1024");
	}

}
