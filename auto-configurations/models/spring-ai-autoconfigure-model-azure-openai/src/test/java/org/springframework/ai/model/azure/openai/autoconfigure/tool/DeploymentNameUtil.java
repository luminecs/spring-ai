package org.springframework.ai.model.azure.openai.autoconfigure.tool;

import org.springframework.util.StringUtils;

public final class DeploymentNameUtil {

	private DeploymentNameUtil() {

	}

	public static String getDeploymentName() {
		String deploymentName = System.getenv("AZURE_OPENAI_DEPLOYMENT_NAME");
		if (StringUtils.hasText(deploymentName)) {
			return deploymentName;
		}
		else {
			return "gpt-4o";
		}
	}

}
