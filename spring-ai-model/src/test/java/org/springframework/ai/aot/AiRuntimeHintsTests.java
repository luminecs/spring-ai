package org.springframework.ai.aot;

import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.TypeReference;
import org.springframework.util.Assert;

class AiRuntimeHintsTests {

	@Test
	void discoverRelevantClasses() throws Exception {
		var classes = AiRuntimeHints.findJsonAnnotatedClassesInPackage(TestApi.class);
		var included = Set.of(TestApi.Bar.class, TestApi.Foo.class)
			.stream()
			.map(t -> TypeReference.of(t.getName()))
			.collect(Collectors.toSet());
		LogFactory.getLog(getClass()).info(classes);
		Assert.state(classes.containsAll(included), "there should be all of the enumerated classes. ");
	}

	@JsonInclude
	static class TestApi {

		@JsonInclude
		enum Bar {

			A, B

		}

		static class FooBar {

		}

		record Foo(@JsonProperty("name") String name) {

		}

	}

}
