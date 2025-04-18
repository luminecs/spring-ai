package org.springframework.ai.vectorstore.testcontainer;

import org.testcontainers.couchbase.BucketDefinition;
import org.testcontainers.utility.DockerImageName;

public final class CouchbaseContainerMetadata {

	public static final String BUCKET_NAME = "springBucket";

	public static final String USERNAME = "Administrator";

	public static final String PASSWORD = "password";

	public static final BucketDefinition bucketDefinition = new BucketDefinition(BUCKET_NAME);

	public static final DockerImageName COUCHBASE_IMAGE_ENTERPRISE = DockerImageName.parse("couchbase:enterprise")
		.asCompatibleSubstituteFor("couchbase/server")
		.withTag("enterprise-7.6.1");

	private CouchbaseContainerMetadata() {

	}

}
