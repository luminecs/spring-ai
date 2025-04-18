package org.springframework.ai.model.bedrock.autoconfigure;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@RequiresAwsCredentials
public class BedrockAwsConnectionConfigurationIT {

	@Test
	public void autoConfigureAWSCredentialAndRegionProvider() {
		BedrockTestUtils.getContextRunner()
			.withConfiguration(AutoConfigurations.of(TestAutoConfiguration.class))
			.run(context -> {
				var awsCredentialsProvider = context.getBean(AwsCredentialsProvider.class);
				var awsRegionProvider = context.getBean(AwsRegionProvider.class);

				assertThat(awsCredentialsProvider).isNotNull();
				assertThat(awsRegionProvider).isNotNull();

				var credentials = awsCredentialsProvider.resolveCredentials();
				assertThat(credentials).isNotNull();
				assertThat(credentials.accessKeyId()).isEqualTo(System.getenv("AWS_ACCESS_KEY_ID"));
				assertThat(credentials.secretAccessKey()).isEqualTo(System.getenv("AWS_SECRET_ACCESS_KEY"));

				assertThat(awsRegionProvider.getRegion()).isEqualTo(Region.US_EAST_1);
			});
	}

	@Test
	public void autoConfigureWithCustomAWSCredentialAndRegionProvider() {
		BedrockTestUtils.getContextRunner()
			.withConfiguration(AutoConfigurations.of(TestAutoConfiguration.class,
					CustomAwsCredentialsProviderAndAwsRegionProviderAutoConfiguration.class))
			.run(context -> {
				var awsCredentialsProvider = context.getBean(AwsCredentialsProvider.class);
				var awsRegionProvider = context.getBean(AwsRegionProvider.class);

				assertThat(awsCredentialsProvider).isNotNull();
				assertThat(awsRegionProvider).isNotNull();

				var credentials = awsCredentialsProvider.resolveCredentials();
				assertThat(credentials).isNotNull();
				assertThat(credentials.accessKeyId()).isEqualTo("CUSTOM_ACCESS_KEY");
				assertThat(credentials.secretAccessKey()).isEqualTo("CUSTOM_SECRET_ACCESS_KEY");

				assertThat(awsRegionProvider.getRegion()).isEqualTo(Region.AWS_GLOBAL);
			});
	}

	@EnableConfigurationProperties({ BedrockAwsConnectionProperties.class })
	@Import(BedrockAwsConnectionConfiguration.class)
	static class TestAutoConfiguration {

	}

	@AutoConfiguration
	static class CustomAwsCredentialsProviderAndAwsRegionProviderAutoConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public AwsCredentialsProvider credentialsProvider() {
			return new AwsCredentialsProvider() {

				@Override
				public AwsCredentials resolveCredentials() {
					return new AwsCredentials() {

						@Override
						public String accessKeyId() {
							return "CUSTOM_ACCESS_KEY";
						}

						@Override
						public String secretAccessKey() {
							return "CUSTOM_SECRET_ACCESS_KEY";
						}

					};
				}

			};
		}

		@Bean
		@ConditionalOnMissingBean
		public AwsRegionProvider regionProvider() {
			return new AwsRegionProvider() {

				@Override
				public Region getRegion() {
					return Region.AWS_GLOBAL;
				}

			};
		}

	}

}
