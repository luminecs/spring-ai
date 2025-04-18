package org.springframework.ai.qianfan.image;

import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;

import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.image.observation.DefaultImageModelObservationConvention;
import org.springframework.ai.observation.conventions.AiOperationType;
import org.springframework.ai.observation.conventions.AiProvider;
import org.springframework.ai.qianfan.QianFanImageModel;
import org.springframework.ai.qianfan.QianFanImageOptions;
import org.springframework.ai.qianfan.api.QianFanImageApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ai.image.observation.ImageModelObservationDocumentation.HighCardinalityKeyNames;
import static org.springframework.ai.image.observation.ImageModelObservationDocumentation.LowCardinalityKeyNames;

@SpringBootTest(classes = QianFanImageModelObservationIT.Config.class)
@EnabledIfEnvironmentVariables({ @EnabledIfEnvironmentVariable(named = "QIANFAN_API_KEY", matches = ".+"),
		@EnabledIfEnvironmentVariable(named = "QIANFAN_SECRET_KEY", matches = ".+") })
public class QianFanImageModelObservationIT {

	@Autowired
	TestObservationRegistry observationRegistry;

	@Autowired
	QianFanImageModel imageModel;

	@Test
	void observationForImageOperation() {
		var options = QianFanImageOptions.builder()
			.model(QianFanImageApi.ImageModel.Stable_Diffusion_XL.getValue())
			.height(1024)
			.width(1024)
			.style("Base")
			.build();

		var instructions = "Here comes the sun";

		ImagePrompt imagePrompt = new ImagePrompt(instructions, options);

		ImageResponse imageResponse = this.imageModel.call(imagePrompt);
		assertThat(imageResponse.getResults()).hasSize(1);

		TestObservationRegistryAssert.assertThat(this.observationRegistry)
			.doesNotHaveAnyRemainingCurrentObservation()
			.hasObservationWithNameEqualTo(DefaultImageModelObservationConvention.DEFAULT_NAME)
			.that()
			.hasContextualNameEqualTo("image " + QianFanImageApi.ImageModel.Stable_Diffusion_XL.getValue())
			.hasLowCardinalityKeyValue(LowCardinalityKeyNames.AI_OPERATION_TYPE.asString(),
					AiOperationType.IMAGE.value())
			.hasLowCardinalityKeyValue(LowCardinalityKeyNames.AI_PROVIDER.asString(), AiProvider.QIANFAN.value())
			.hasLowCardinalityKeyValue(LowCardinalityKeyNames.REQUEST_MODEL.asString(),
					QianFanImageApi.ImageModel.Stable_Diffusion_XL.getValue())
			.hasHighCardinalityKeyValue(HighCardinalityKeyNames.REQUEST_IMAGE_SIZE.asString(), "1024x1024")
			.hasHighCardinalityKeyValue(HighCardinalityKeyNames.REQUEST_IMAGE_STYLE.asString(), "Base")
			.hasBeenStarted()
			.hasBeenStopped();
	}

	@SpringBootConfiguration
	static class Config {

		@Bean
		public TestObservationRegistry observationRegistry() {
			return TestObservationRegistry.create();
		}

		@Bean
		public QianFanImageApi qianFanImageApi() {
			return new QianFanImageApi(System.getenv("QIANFAN_API_KEY"), System.getenv("QIANFAN_SECRET_KEY"));
		}

		@Bean
		public QianFanImageModel qianFanImageModel(QianFanImageApi qianFanImageApi,
				TestObservationRegistry observationRegistry) {
			return new QianFanImageModel(qianFanImageApi, QianFanImageOptions.builder().build(),
					RetryTemplate.defaultInstance(), observationRegistry);
		}

	}

}
