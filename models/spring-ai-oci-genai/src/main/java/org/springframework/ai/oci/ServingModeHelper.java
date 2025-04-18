package org.springframework.ai.oci;

import com.oracle.bmc.generativeaiinference.model.DedicatedServingMode;
import com.oracle.bmc.generativeaiinference.model.OnDemandServingMode;
import com.oracle.bmc.generativeaiinference.model.ServingMode;

public final class ServingModeHelper {

	private ServingModeHelper() {
	}

	public static ServingMode get(String servingMode, String model) {
		return switch (servingMode) {
			case "dedicated" -> DedicatedServingMode.builder().endpointId(model).build();
			case "on-demand" -> OnDemandServingMode.builder().modelId(model).build();
			default -> throw new IllegalArgumentException(String.format(
					"Unknown serving mode for OCI Gen AI: %s. Supported options are 'dedicated' and 'on-demand'",
					servingMode));
		};
	}

}
