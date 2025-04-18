package org.springframework.ai.vectorstore.pgvector;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PgVectorStoreTests {

	@ParameterizedTest(name = "{0} - Verifies valid Table name")
	@CsvSource({

			"customvectorstore, true", "user_data, true", "test123, true", "valid_table_name, true",

			"'', false", "   , false", "custom vector store, false", "customvectorstore;, false",
			"customvectorstore--, false", "drop table users;, false", "customvectorstore;drop table users;, false",

			"customvectorstore#, false", "customvectorstore$, false", "1, false", "customvectorstore or 1=1, false",
			"customvectorstore;--, false", "custom_vector_store; DROP TABLE users;, false",
			"'customvectorstore\u0000', false", "'customvectorstore\n', false",
			"12345678901234567890123456789012345678901234567890123456789012345, false"

	})
	void isValidTable(String tableName, Boolean expected) {
		assertThat(PgVectorSchemaValidator.isValidNameForDatabaseObject(tableName)).isEqualTo(expected);
	}

	@Test
	void shouldAddDocumentsInBatchesAndEmbedOnce() {

		var jdbcTemplate = mock(JdbcTemplate.class);
		var embeddingModel = mock(EmbeddingModel.class);
		var pgVectorStore = PgVectorStore.builder(jdbcTemplate, embeddingModel).maxDocumentBatchSize(1000).build();

		var documents = Collections.nCopies(9989, new Document("foo"));

		pgVectorStore.doAdd(documents);

		verify(embeddingModel, only()).embed(eq(documents), any(), any());

		var batchUpdateCaptor = ArgumentCaptor.forClass(BatchPreparedStatementSetter.class);
		verify(jdbcTemplate, times(10)).batchUpdate(anyString(), batchUpdateCaptor.capture());

		assertThat(batchUpdateCaptor.getAllValues()).hasSize(10)
			.allSatisfy(BatchPreparedStatementSetter::getBatchSize)
			.satisfies(batches -> {
				for (int i = 0; i < 9; i++) {
					assertThat(batches.get(i).getBatchSize()).as("Batch at index %d should have size 10", i)
						.isEqualTo(1000);
				}
				assertThat(batches.get(9).getBatchSize()).as("Last batch should have size 989").isEqualTo(989);
			});
	}

}
