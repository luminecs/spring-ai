package org.springframework.ai.vectorstore.hanadb.autoconfigure;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

public class HanaCloudVectorStorePropertiesTest {

	@Test
	public void testHanaCloudVectorStoreProperties() {
		var props = new HanaCloudVectorStoreProperties();
		props.setTableName("CRICKET_WORLD_CUP");
		props.setTopK(5);

		Assertions.assertEquals("CRICKET_WORLD_CUP", props.getTableName());
		Assertions.assertEquals(5, props.getTopK());
	}

}
