package org.springframework.ai.vectorstore.opensearch.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OpenSearchNonAwsCondition extends SpringBootCondition {

	private static final String AWS_ENABLED_PROPERTY = "spring.ai.vectorstore.opensearch.aws.enabled";

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {

		String awsEnabled = context.getEnvironment().getProperty(AWS_ENABLED_PROPERTY);
		if ("false".equalsIgnoreCase(awsEnabled)) {
			return ConditionOutcome.match(ConditionMessage.forCondition("OpenSearchNonAwsCondition")
				.because("Property 'spring.ai.vectorstore.opensearch.aws.enabled' is false"));
		}

		boolean awsClassesPresent = isPresent("software.amazon.awssdk.auth.credentials.AwsCredentialsProvider")
				&& isPresent("software.amazon.awssdk.regions.Region")
				&& isPresent("software.amazon.awssdk.http.apache.ApacheHttpClient");
		if (!awsClassesPresent) {
			return ConditionOutcome.match(
					ConditionMessage.forCondition("OpenSearchNonAwsCondition").because("AWS SDK classes are missing"));
		}

		return ConditionOutcome.noMatch(ConditionMessage.forCondition("OpenSearchNonAwsCondition")
			.because("AWS SDK classes are present and property is not false"));
	}

	private boolean isPresent(String className) {
		try {
			Class.forName(className, false, getClass().getClassLoader());
			return true;
		}
		catch (ClassNotFoundException ex) {
			return false;
		}
	}

}
