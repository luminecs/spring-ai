package org.springframework.ai.document.id;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IdGeneratorProviderTest {

	@Test
	void hashGeneratorGenerateSimilarIdsForSimilarContent() {

		var idGenerator1 = new JdkSha256HexIdGenerator();
		var idGenerator2 = new JdkSha256HexIdGenerator();

		final String content = "Content";
		final Map<String, Object> metadata = Map.of("metadata", Set.of("META_DATA"));

		String actualHashes1 = idGenerator1.generateId(content, metadata);
		String actualHashes2 = idGenerator2.generateId(content, metadata);

		Assertions.assertEquals(actualHashes1, actualHashes2);

		Assertions.assertDoesNotThrow(() -> UUID.fromString(actualHashes1));
		Assertions.assertDoesNotThrow(() -> UUID.fromString(actualHashes2));
	}

	@Test
	void hashGeneratorGenerateDifferentIdsForDifferentContent() {

		var idGenerator1 = new JdkSha256HexIdGenerator();
		var idGenerator2 = new JdkSha256HexIdGenerator();

		final String content1 = "Content";
		final Map<String, Object> metadata1 = Map.of("metadata", Set.of("META_DATA"));
		final String content2 = content1 + " ";
		final Map<String, Object> metadata2 = metadata1;

		String actualHashes1 = idGenerator1.generateId(content1, metadata1);
		String actualHashes2 = idGenerator2.generateId(content2, metadata2);

		Assertions.assertNotEquals(actualHashes1, actualHashes2);

		Assertions.assertDoesNotThrow(() -> UUID.fromString(actualHashes1));
		Assertions.assertDoesNotThrow(() -> UUID.fromString(actualHashes2));
	}

}
