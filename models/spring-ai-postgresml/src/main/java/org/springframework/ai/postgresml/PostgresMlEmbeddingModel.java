package org.springframework.ai.postgresml;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingResponseMetadata;
import org.springframework.ai.model.EmbeddingUtils;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class PostgresMlEmbeddingModel extends AbstractEmbeddingModel implements InitializingBean {

	public static final String DEFAULT_TRANSFORMER_MODEL = "distilbert-base-uncased";

	private final PostgresMlEmbeddingOptions defaultOptions;

	private final JdbcTemplate jdbcTemplate;

	private final boolean createExtension;

	public PostgresMlEmbeddingModel(JdbcTemplate jdbcTemplate) {
		this(jdbcTemplate, PostgresMlEmbeddingOptions.builder().build(), false);
	}

	public PostgresMlEmbeddingModel(JdbcTemplate jdbcTemplate, PostgresMlEmbeddingOptions options) {
		this(jdbcTemplate, options, false);
	}

	public PostgresMlEmbeddingModel(JdbcTemplate jdbcTemplate, PostgresMlEmbeddingOptions options,
			boolean createExtension) {
		Assert.notNull(jdbcTemplate, "jdbc template must not be null.");
		Assert.notNull(options, "options must not be null.");
		Assert.notNull(options.getTransformer(), "transformer must not be null.");
		Assert.notNull(options.getVectorType(), "vectorType must not be null.");
		Assert.notNull(options.getKwargs(), "kwargs must not be null.");
		Assert.notNull(options.getMetadataMode(), "metadataMode must not be null.");

		this.jdbcTemplate = jdbcTemplate;
		this.defaultOptions = options;
		this.createExtension = createExtension;
	}

	@SuppressWarnings("null")
	@Override
	public float[] embed(String text) {
		return this.jdbcTemplate.queryForObject(
				"SELECT pgml.embed(?, ?, ?::JSONB)" + this.defaultOptions.getVectorType().cast + " AS embedding",
				this.defaultOptions.getVectorType().rowMapper, this.defaultOptions.getTransformer(), text,
				ModelOptionsUtils.toJsonString(this.defaultOptions.getKwargs()));
	}

	@Override
	public float[] embed(Document document) {
		return this.embed(document.getFormattedContent(this.defaultOptions.getMetadataMode()));
	}

	@SuppressWarnings("null")
	@Override
	public EmbeddingResponse call(EmbeddingRequest request) {

		final PostgresMlEmbeddingOptions optionsToUse = this.mergeOptions(request.getOptions());

		List<Embedding> data = new ArrayList<>();
		List<float[]> embed = List.of();

		List<String> texts = request.getInstructions();
		if (!CollectionUtils.isEmpty(texts)) {
			embed = this.jdbcTemplate.query(connection -> {
				PreparedStatement preparedStatement = connection.prepareStatement("SELECT pgml.embed(?, text, ?::JSONB)"
						+ optionsToUse.getVectorType().cast + " AS embedding FROM (SELECT unnest(?) AS text) AS texts");
				preparedStatement.setString(1, optionsToUse.getTransformer());
				preparedStatement.setString(2, ModelOptionsUtils.toJsonString(optionsToUse.getKwargs()));
				preparedStatement.setArray(3, connection.createArrayOf("TEXT", texts.toArray(Object[]::new)));
				return preparedStatement;
			}, rs -> {
				List<float[]> result = new ArrayList<>();
				while (rs.next()) {
					result.add(optionsToUse.getVectorType().rowMapper.mapRow(rs, -1));
				}
				return result;
			});
		}

		if (!CollectionUtils.isEmpty(embed)) {
			for (int i = 0; i < embed.size(); i++) {
				data.add(new Embedding(embed.get(i), i));
			}
		}

		Map<String, Object> embeddingMetadata = Map.of("transformer", optionsToUse.getTransformer(), "vector-type",
				optionsToUse.getVectorType().name(), "kwargs",
				ModelOptionsUtils.toJsonString(optionsToUse.getKwargs()));
		var embeddingResponseMetadata = new EmbeddingResponseMetadata("unknown", new EmptyUsage(), embeddingMetadata);
		return new EmbeddingResponse(data, embeddingResponseMetadata);
	}

	PostgresMlEmbeddingOptions mergeOptions(EmbeddingOptions requestOptions) {

		PostgresMlEmbeddingOptions options = (this.defaultOptions != null) ? this.defaultOptions
				: PostgresMlEmbeddingOptions.builder().build();

		if (requestOptions != null) {
			options = ModelOptionsUtils.merge(requestOptions, options, PostgresMlEmbeddingOptions.class);
		}

		return options;
	}

	@Override
	public void afterPropertiesSet() {
		if (!this.createExtension) {
			return;
		}
		this.jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS pgml");
		if (StringUtils.hasText(this.defaultOptions.getVectorType().extensionName)) {
			this.jdbcTemplate
				.execute("CREATE EXTENSION IF NOT EXISTS " + this.defaultOptions.getVectorType().extensionName);
		}
	}

	public enum VectorType {

		PG_ARRAY("", null, (rs, i) -> {
			Array embedding = rs.getArray("embedding");
			return EmbeddingUtils.toPrimitive((Float[]) embedding.getArray());

		}),

		PG_VECTOR("::vector", "vector", (rs, i) -> {
			String embedding = rs.getString("embedding");
			return EmbeddingUtils.toPrimitive(Arrays.stream((embedding.substring(1, embedding.length() - 1).split(",")))
				.map(Float::parseFloat)
				.toList());
		});

		private final String cast;

		private final String extensionName;

		private final RowMapper<float[]> rowMapper;

		VectorType(String cast, String extensionName, RowMapper<float[]> rowMapper) {
			this.cast = cast;
			this.extensionName = extensionName;
			this.rowMapper = rowMapper;
		}

	}

}
