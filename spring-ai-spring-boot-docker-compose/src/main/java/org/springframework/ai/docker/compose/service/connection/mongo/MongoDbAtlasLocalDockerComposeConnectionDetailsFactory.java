package org.springframework.ai.docker.compose.service.connection.mongo;

import com.mongodb.ConnectionString;

import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class MongoDbAtlasLocalDockerComposeConnectionDetailsFactory
		extends DockerComposeConnectionDetailsFactory<MongoConnectionDetails> {

	private static final int MONGODB_PORT = 27017;

	protected MongoDbAtlasLocalDockerComposeConnectionDetailsFactory() {
		super("mongodb/mongodb-atlas-local");
	}

	@Override
	protected MongoConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
		return new MongoDbAtlasLocalContainerConnectionDetails(source.getRunningService());
	}

	static class MongoDbAtlasLocalContainerConnectionDetails extends DockerComposeConnectionDetails
			implements MongoConnectionDetails {

		private final String connectionString;

		MongoDbAtlasLocalContainerConnectionDetails(RunningService service) {
			super(service);
			this.connectionString = String.format("mongodb://%s:%d/?directConnection=true", service.host(),
					service.ports().get(MONGODB_PORT));
		}

		@Override
		public ConnectionString getConnectionString() {
			return new ConnectionString(this.connectionString);
		}

	}

}
