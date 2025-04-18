package org.springframework.ai.chroma.vectorstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.ai.chroma.vectorstore.ChromaApi.QueryRequest.Include;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

public class ChromaApi {

	private static final Pattern VALUE_ERROR_PATTERN = Pattern.compile("ValueError\\('([^']*)'\\)");

	private static final Pattern MESSAGE_ERROR_PATTERN = Pattern.compile("\"message\":\"(.*?)\"");

	private final ObjectMapper objectMapper;

	private RestClient restClient;

	@Nullable
	private String keyToken;

	public ChromaApi(String baseUrl) {
		this(baseUrl, RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory()), new ObjectMapper());
	}

	public ChromaApi(String baseUrl, RestClient.Builder restClientBuilder) {
		this(baseUrl, restClientBuilder, new ObjectMapper());
	}

	public ChromaApi(String baseUrl, RestClient.Builder restClientBuilder, ObjectMapper objectMapper) {

		this.restClient = restClientBuilder.baseUrl(baseUrl)
			.defaultHeaders(h -> h.setContentType(MediaType.APPLICATION_JSON))
			.build();
		this.objectMapper = objectMapper;
	}

	public ChromaApi withKeyToken(String keyToken) {
		this.keyToken = keyToken;
		return this;
	}

	public ChromaApi withBasicAuthCredentials(String username, String password) {
		this.restClient = this.restClient.mutate()
			.requestInterceptor(new BasicAuthenticationInterceptor(username, password))
			.build();
		return this;
	}

	public List<Embedding> toEmbeddingResponseList(@Nullable QueryResponse queryResponse) {
		List<Embedding> result = new ArrayList<>();

		if (queryResponse != null && !CollectionUtils.isEmpty(queryResponse.ids())) {
			for (int i = 0; i < queryResponse.ids().get(0).size(); i++) {
				result.add(new Embedding(queryResponse.ids().get(0).get(i), queryResponse.embeddings().get(0).get(i),
						queryResponse.documents().get(0).get(i), queryResponse.metadata().get(0).get(i),
						queryResponse.distances().get(0).get(i)));
			}
		}

		return result;
	}

	@Nullable
	public Collection createCollection(CreateCollectionRequest createCollectionRequest) {

		return this.restClient.post()
			.uri("/api/v1/collections")
			.headers(this::httpHeaders)
			.body(createCollectionRequest)
			.retrieve()
			.toEntity(Collection.class)
			.getBody();
	}

	public void deleteCollection(String collectionName) {

		this.restClient.delete()
			.uri("/api/v1/collections/{collection_name}", collectionName)
			.headers(this::httpHeaders)
			.retrieve()
			.toBodilessEntity();
	}

	@Nullable
	public Collection getCollection(String collectionName) {

		try {
			return this.restClient.get()
				.uri("/api/v1/collections/{collection_name}", collectionName)
				.headers(this::httpHeaders)
				.retrieve()
				.toEntity(Collection.class)
				.getBody();
		}
		catch (HttpServerErrorException | HttpClientErrorException e) {
			String msg = this.getErrorMessage(e);
			if (String.format("Collection %s does not exist.", collectionName).equals(msg)) {
				return null;
			}
			throw new RuntimeException(msg, e);
		}
	}

	@Nullable
	public List<Collection> listCollections() {

		return this.restClient.get()
			.uri("/api/v1/collections")
			.headers(this::httpHeaders)
			.retrieve()
			.toEntity(CollectionList.class)
			.getBody();
	}

	public void upsertEmbeddings(@Nullable String collectionId, AddEmbeddingsRequest embedding) {

		this.restClient.post()
			.uri("/api/v1/collections/{collection_id}/upsert", collectionId)
			.headers(this::httpHeaders)
			.body(embedding)
			.retrieve()
			.toBodilessEntity();
	}

	public int deleteEmbeddings(@Nullable String collectionId, DeleteEmbeddingsRequest deleteRequest) {
		return this.restClient.post()
			.uri("/api/v1/collections/{collection_id}/delete", collectionId)
			.headers(this::httpHeaders)
			.body(deleteRequest)
			.retrieve()
			.toEntity(String.class)
			.getStatusCode()
			.value();
	}

	@Nullable
	public Long countEmbeddings(String collectionId) {

		return this.restClient.get()
			.uri("/api/v1/collections/{collection_id}/count", collectionId)
			.headers(this::httpHeaders)
			.retrieve()
			.toEntity(Long.class)
			.getBody();
	}

	@Nullable
	public QueryResponse queryCollection(@Nullable String collectionId, QueryRequest queryRequest) {

		return this.restClient.post()
			.uri("/api/v1/collections/{collection_id}/query", collectionId)
			.headers(this::httpHeaders)
			.body(queryRequest)
			.retrieve()
			.toEntity(QueryResponse.class)
			.getBody();
	}

	@Nullable
	public GetEmbeddingResponse getEmbeddings(String collectionId, GetEmbeddingsRequest getEmbeddingsRequest) {

		return this.restClient.post()
			.uri("/api/v1/collections/{collection_id}/get", collectionId)
			.headers(this::httpHeaders)
			.body(getEmbeddingsRequest)
			.retrieve()
			.toEntity(GetEmbeddingResponse.class)
			.getBody();
	}

	public Map<String, Object> where(String text) {
		try {
			return this.objectMapper.readValue(text, Map.class);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	private void httpHeaders(HttpHeaders headers) {
		if (StringUtils.hasText(this.keyToken)) {
			headers.setBearerAuth(this.keyToken);
		}
	}

	private String getErrorMessage(HttpStatusCodeException e) {
		var errorMessage = e.getMessage();

		if (!StringUtils.hasText(errorMessage)) {
			return "";
		}

		Matcher valueErrorMatcher = VALUE_ERROR_PATTERN.matcher(errorMessage);
		if (e instanceof HttpServerErrorException && valueErrorMatcher.find()) {
			return valueErrorMatcher.group(1);
		}

		Matcher messageErrorMatcher = MESSAGE_ERROR_PATTERN.matcher(errorMessage);
		if (messageErrorMatcher.find()) {
			return messageErrorMatcher.group(1);
		}

		return "";
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Collection(// @formatter:off
		@JsonProperty("id") String id,
		@JsonProperty("name") String name,
		@JsonProperty("metadata") Map<String, Object> metadata) { // @formatter:on

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record CreateCollectionRequest(// @formatter:off
		@JsonProperty("name") String name,
		@JsonProperty("metadata") Map<String, Object> metadata) { // @formatter:on

		public CreateCollectionRequest(String name) {
			this(name, new HashMap<>(Map.of("hnsw:space", "cosine")));
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record AddEmbeddingsRequest(// @formatter:off
			@JsonProperty("ids") List<String> ids,
			@JsonProperty("embeddings") List<float[]> embeddings,
			@JsonProperty("metadatas") List<Map<String, Object>> metadata,
			@JsonProperty("documents") List<String> documents) { // @formatter:on

		public AddEmbeddingsRequest(String id, float[] embedding, Map<String, Object> metadata, String document) {
			this(List.of(id), List.of(embedding), List.of(metadata), List.of(document));
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record DeleteEmbeddingsRequest(// @formatter:off
		@Nullable @JsonProperty("ids") List<String> ids,
		@Nullable @JsonProperty("where") Map<String, Object> where) { // @formatter:on

		public DeleteEmbeddingsRequest(List<String> ids) {
			this(ids, null);
		}
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record GetEmbeddingsRequest(// @formatter:off
		@JsonProperty("ids") List<String> ids,
		@Nullable @JsonProperty("where") Map<String, Object> where,
		@JsonProperty("limit") Integer limit,
		@JsonProperty("offset") Integer offset,
		@JsonProperty("include") List<Include> include) { // @formatter:on

		public GetEmbeddingsRequest(List<String> ids) {
			this(ids, null, 10, 0, Include.all);
		}

		public GetEmbeddingsRequest(List<String> ids, Map<String, Object> where) {
			this(ids, CollectionUtils.isEmpty(where) ? null : where, 10, 0, Include.all);
		}

		public GetEmbeddingsRequest(List<String> ids, Map<String, Object> where, Integer limit, Integer offset) {
			this(ids, CollectionUtils.isEmpty(where) ? null : where, limit, offset, Include.all);
		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record GetEmbeddingResponse(// @formatter:off
		@JsonProperty("ids") List<String> ids,
		@JsonProperty("embeddings") List<float[]> embeddings,
		@JsonProperty("documents") List<String> documents,
		@JsonProperty("metadatas") List<Map<String, String>> metadata) { // @formatter:on
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record QueryRequest(// @formatter:off
		@JsonProperty("query_embeddings") List<float[]> queryEmbeddings,
		@JsonProperty("n_results") Integer nResults,
		@Nullable @JsonProperty("where") Map<String, Object> where,
		@JsonProperty("include") List<Include> include) { // @formatter:on

		public QueryRequest(float[] queryEmbedding, Integer nResults) {
			this(List.of(queryEmbedding), nResults, null, Include.all);
		}

		public QueryRequest(float[] queryEmbedding, Integer nResults, @Nullable Map<String, Object> where) {
			this(List.of(queryEmbedding), nResults, CollectionUtils.isEmpty(where) ? null : where, Include.all);
		}

		public enum Include {

			@JsonProperty("metadatas")
			METADATAS,

			@JsonProperty("documents")
			DOCUMENTS,

			@JsonProperty("distances")
			DISTANCES,

			@JsonProperty("embeddings")
			EMBEDDINGS;

			public static final List<Include> all = List.of(METADATAS, DOCUMENTS, DISTANCES, EMBEDDINGS);

		}

	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record QueryResponse(// @formatter:off
		@JsonProperty("ids") List<List<String>> ids,
		@JsonProperty("embeddings") List<List<float[]>> embeddings,
		@JsonProperty("documents") List<List<String>> documents,
		@JsonProperty("metadatas") List<List<Map<String, Object>>> metadata,
		@JsonProperty("distances") List<List<Double>> distances) { // @formatter:on
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Embedding(// @formatter:off
		@JsonProperty("id") String id,
		@JsonProperty("embedding") float[] embedding,
		@JsonProperty("document") String document,
		@Nullable @JsonProperty("metadata") Map<String, Object> metadata,
		@JsonProperty("distances") Double distances) { // @formatter:on

	}

	private static class CollectionList extends ArrayList<Collection> {

	}

}
