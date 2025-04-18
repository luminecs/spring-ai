package org.springframework.ai.model.oci.genai.autoconfigure;

import java.io.IOException;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.auth.okeworkloadidentity.OkeWorkloadIdentityAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.retrier.RetryConfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

@AutoConfiguration
@ConditionalOnClass(GenerativeAiInferenceClient.class)
@EnableConfigurationProperties(OCIConnectionProperties.class)
public class OCIGenAiInferenceClientAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public GenerativeAiInferenceClient generativeAiInferenceClient(OCIConnectionProperties properties)
			throws IOException {
		ClientConfiguration clientConfiguration = ClientConfiguration.builder()
			.retryConfiguration(RetryConfiguration.SDK_DEFAULT_RETRY_CONFIGURATION)
			.build();
		GenerativeAiInferenceClient.Builder builder = GenerativeAiInferenceClient.builder()
			.configuration(clientConfiguration);
		if (StringUtils.hasText(properties.getRegion())) {
			builder.region(Region.valueOf(properties.getRegion()));
		}
		if (StringUtils.hasText(properties.getEndpoint())) {
			builder.endpoint(properties.getEndpoint());
		}
		return builder.build(authenticationProvider(properties));
	}

	private static BasicAuthenticationDetailsProvider authenticationProvider(OCIConnectionProperties properties)
			throws IOException {
		return switch (properties.getAuthenticationType()) {
			case FILE -> new ConfigFileAuthenticationDetailsProvider(properties.getFile(), properties.getProfile());
			case INSTANCE_PRINCIPAL -> InstancePrincipalsAuthenticationDetailsProvider.builder().build();
			case WORKLOAD_IDENTITY -> OkeWorkloadIdentityAuthenticationDetailsProvider.builder().build();
			case SIMPLE -> SimpleAuthenticationDetailsProvider.builder()
				.userId(properties.getUserId())
				.tenantId(properties.getTenantId())
				.fingerprint(properties.getFingerprint())
				.privateKeySupplier(new SimplePrivateKeySupplier(properties.getPrivateKey()))
				.passPhrase(properties.getPassPhrase())
				.region(Region.valueOf(properties.getRegion()))
				.build();
		};
	}

}
