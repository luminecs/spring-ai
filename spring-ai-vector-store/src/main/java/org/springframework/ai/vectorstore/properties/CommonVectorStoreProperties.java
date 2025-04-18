package org.springframework.ai.vectorstore.properties;

public class CommonVectorStoreProperties {

	private boolean initializeSchema = false;

	public boolean isInitializeSchema() {
		return this.initializeSchema;
	}

	public void setInitializeSchema(boolean initializeSchema) {
		this.initializeSchema = initializeSchema;
	}

}
