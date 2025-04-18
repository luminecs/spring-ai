package org.springframework.ai.testcontainers.service.connection.mongo;

import java.lang.reflect.Method;

import com.mongodb.ConnectionString;
import org.testcontainers.mongodb.MongoDBAtlasLocalContainer;

import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.springframework.util.ReflectionUtils;

class MongoDbAtlasLocalContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<MongoDBAtlasLocalContainer, MongoConnectionDetails> {

	private static final Method GET_SSL_BUNDLE_METHOD;

	static {
		GET_SSL_BUNDLE_METHOD = ReflectionUtils.findMethod(MongoConnectionDetails.class, "getSslBundle");
	}

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

		public SslBundle getSslBundle() {
			if (GET_SSL_BUNDLE_METHOD != null) {
				return (SslBundle) ReflectionUtils.invokeMethod(GET_SSL_BUNDLE_METHOD, this);
			}
			return null;
		}

	}

}
