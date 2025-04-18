package org.springframework.ai.chat.memory.cassandra;

import java.time.Duration;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SchemaUtil {

	private static final Logger logger = LoggerFactory.getLogger(SchemaUtil.class);

	private SchemaUtil() {

	}

	public static void checkSchemaAgreement(CqlSession session) throws IllegalStateException {
		if (!session.checkSchemaAgreement()) {
			logger.warn("Waiting for cluster schema agreement, sleeping 10s…");
			try {
				Thread.sleep(Duration.ofSeconds(10).toMillis());
			}
			catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(ex);
			}
			if (!session.checkSchemaAgreement()) {
				logger.error("no cluster schema agreement still, continuing, let's hope this works…");
			}
		}
	}

	public static void ensureKeyspaceExists(CqlSession session, String keyspaceName) {
		if (session.getMetadata().getKeyspace(keyspaceName).isEmpty()) {
			SimpleStatement keyspaceStmt = SchemaBuilder.createKeyspace(keyspaceName)
				.ifNotExists()
				.withSimpleStrategy(1)
				.build();

			logger.debug("Executing {}", keyspaceStmt.getQuery());
			session.execute(keyspaceStmt);
		}
	}

}
