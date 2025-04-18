package org.springframework.ai.vectorstore.mariadb;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.mariadb.jdbc.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class MariaDBSchemaValidator {

	private static final Logger logger = LoggerFactory.getLogger(MariaDBSchemaValidator.class);

	private final JdbcTemplate jdbcTemplate;

	public MariaDBSchemaValidator(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private boolean isTableExists(String schemaName, String tableName) {

		String sql = String.format(
				"SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = %s AND TABLE_NAME = %s",
				(schemaName == null) ? "SCHEMA()" : schemaName, tableName);
		try {

			this.jdbcTemplate.queryForObject(sql, Integer.class);
			return true;
		}
		catch (DataAccessException e) {
			return false;
		}
	}

	void validateTableSchema(String schemaName, String tableName, String idFieldName, String contentFieldName,
			String metadataFieldName, String embeddingFieldName, int embeddingDimensions) {

		if (!isTableExists(schemaName, tableName)) {
			throw new IllegalStateException(
					String.format("Table '%s' does not exist in schema '%s'", tableName, schemaName));
		}

		try {

			this.jdbcTemplate.queryForObject("SELECT vec_distance_euclidean(x'0000803f', x'0000803f')", Integer.class,
					schemaName, tableName);
		}
		catch (DataAccessException e) {
			logger.error("Error while validating database vector support " + e.getMessage());
			logger.error("Failed to validate that database supports VECTOR.\n" + "Run the following SQL commands:\n"
					+ "   SELECT @@version; \nAnd ensure that version is >= 11.7.1");
			throw new IllegalStateException(e);
		}

		try {
			logger.info("Validating MariaDBStore schema for table: {} in schema: {}", tableName, schemaName);

			List<String> expectedColumns = new ArrayList<>();
			expectedColumns.add(idFieldName);
			expectedColumns.add(contentFieldName);
			expectedColumns.add(metadataFieldName);
			expectedColumns.add(embeddingFieldName);

			String query = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS "
					+ "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
			List<Map<String, Object>> columns = this.jdbcTemplate.queryForList(query, schemaName, tableName);

			if (columns.isEmpty()) {
				throw new IllegalStateException("Error while validating table schema, Table " + tableName
						+ " does not exist in schema " + schemaName);
			}

			List<String> availableColumns = new ArrayList<>();
			for (Map<String, Object> column : columns) {
				String columnName = validateAndEnquoteIdentifier((String) column.get("COLUMN_NAME"), false);
				availableColumns.add(columnName);
			}

			expectedColumns.removeAll(availableColumns);

			if (expectedColumns.isEmpty()) {
				logger.info("MariaDB VectorStore schema validation successful");
			}
			else {
				throw new IllegalStateException("Missing fields " + expectedColumns);
			}

		}
		catch (DataAccessException | IllegalStateException e) {
			logger.error("Error while validating table schema" + e.getMessage());
			logger.error("Failed to operate with the specified table in the database. To resolve this issue,"
					+ " please ensure the following steps are completed:\n"
					+ "1. Verify that the table exists with the appropriate structure. If it does not"
					+ " exist, create it using a SQL command similar to the following:\n"
					+ String.format("""
							  CREATE TABLE IF NOT EXISTS %s (
									%s UUID NOT NULL DEFAULT uuid() PRIMARY KEY,
									%s TEXT,
									%s JSON,
									%s VECTOR(%d) NOT NULL,
									VECTOR INDEX (%s)
							) ENGINE=InnoDB""", schemaName == null ? tableName : schemaName + "." + tableName,
							idFieldName, contentFieldName, metadataFieldName, embeddingFieldName, embeddingDimensions,
							embeddingFieldName)
					+ "\n" + "Please adjust these commands based on your specific configuration and the"
					+ " capabilities of your vector database system.");
			throw new IllegalStateException(e);
		}
	}

	public static String validateAndEnquoteIdentifier(String identifier, boolean alwaysQuote) {
		try {
			String quotedId = Driver.enquoteIdentifier(identifier, alwaysQuote);

			if (Pattern.compile("`?[\\p{Alnum}_]*`?").matcher(identifier).matches()) {
				return quotedId;
			}
			throw new IllegalArgumentException(String
				.format("Identifier '%s' should only contain alphanumeric characters and underscores", quotedId));
		}
		catch (SQLException e) {
			throw new IllegalArgumentException(e);
		}
	}

}
