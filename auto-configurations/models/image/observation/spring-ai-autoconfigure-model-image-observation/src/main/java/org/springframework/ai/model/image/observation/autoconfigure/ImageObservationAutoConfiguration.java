package org.springframework.ai.model.image.observation.autoconfigure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.observation.ImageModelPromptContentObservationFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(
		afterName = "org.springframework.boot.actuate.autoconfigure.observation.ObservationAutoConfiguration.class")
@ConditionalOnClass(ImageModel.class)
@EnableConfigurationProperties({ ImageObservationProperties.class })
public class ImageObservationAutoConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(ImageObservationAutoConfiguration.class);

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty(prefix = ImageObservationProperties.CONFIG_PREFIX, name = "include-prompt",
			havingValue = "true")
	ImageModelPromptContentObservationFilter imageModelPromptObservationFilter() {
		logger.warn(
				"You have enabled the inclusion of the image prompt content in the observations, with the risk of exposing sensitive or private information. Please, be careful!");
		return new ImageModelPromptContentObservationFilter();
	}

}
