package org.springframework.ai.test.vectorstore;

import io.micrometer.observation.tck.TestObservationRegistry;
import io.micrometer.observation.tck.TestObservationRegistryAssert;

import org.springframework.ai.observation.conventions.VectorStoreProvider;
import org.springframework.ai.vectorstore.observation.DefaultVectorStoreObservationConvention;
import org.springframework.ai.vectorstore.observation.VectorStoreObservationContext;

public final class ObservationTestUtil {

	private ObservationTestUtil() {

	}

	public static void assertObservationRegistry(TestObservationRegistry observationRegistry,
			VectorStoreProvider vectorStoreProvider, VectorStoreObservationContext.Operation operation) {
		TestObservationRegistryAssert.assertThat(observationRegistry)
			.doesNotHaveAnyRemainingCurrentObservation()
			.hasObservationWithNameEqualTo(DefaultVectorStoreObservationConvention.DEFAULT_NAME)
			.that()
			.hasContextualNameEqualTo(vectorStoreProvider.value() + " " + operation.value())
			.hasBeenStarted()
			.hasBeenStopped();
	}

}
