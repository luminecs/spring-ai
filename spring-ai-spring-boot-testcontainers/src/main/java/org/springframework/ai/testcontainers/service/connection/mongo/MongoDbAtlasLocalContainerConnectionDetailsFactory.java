package org.springframework.ai.testcontainers.service.connection.mongo;

import com.mongodb.ConnectionString;
import org.testcontainers.mongodb.MongoDBAtlasLocalContainer;

import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;

class MongoDbAtlasLocalContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<MongoDBAtlasLocalContainer, MongoConnectionDetails> {

	@Override
	protected MongoConnectionDetails getContainerConnectionDetails(
			ContainerConnectionSource<MongoDBAtlasLocalContainer> source) {
		return new MongoDbAtlasLocalContainerConnectionDetails(source);
	}

	private static final class MongoDbAtlasLocalContainerConnectionDetails
			extends ContainerConnectionDetails<MongoDBAtlasLocalContainer> implements MongoConnectionDetails {

		private MongoDbAtlasLocalContainerConnectionDetails(
				ContainerConnectionSource<MongoDBAtlasLocalContainer> source) {
			super(source);
		}

		@Override
		public ConnectionString getConnectionString() {
			return new ConnectionString(getContainer().getConnectionString());
		}

	}

}
