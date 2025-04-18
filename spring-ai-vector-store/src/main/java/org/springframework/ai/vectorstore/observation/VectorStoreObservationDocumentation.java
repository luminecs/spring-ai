package org.springframework.ai.vectorstore.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

import org.springframework.ai.observation.conventions.VectorStoreObservationAttributes;

public enum VectorStoreObservationDocumentation implements ObservationDocumentation {

	AI_VECTOR_STORE {
		@Override
		public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
			return DefaultVectorStoreObservationConvention.class;
		}

		@Override
		public KeyName[] getLowCardinalityKeyNames() {
			return LowCardinalityKeyNames.values();
		}

		@Override
		public KeyName[] getHighCardinalityKeyNames() {
			return HighCardinalityKeyNames.values();
		}
	};

	public enum LowCardinalityKeyNames implements KeyName {

		SPRING_AI_KIND {
			@Override
			public String asString() {
				return "spring.ai.kind";
			}
		},

		DB_OPERATION_NAME {
			@Override
			public String asString() {
				return VectorStoreObservationAttributes.DB_OPERATION_NAME.value();
			}
		},

		DB_SYSTEM {
			@Override
			public String asString() {
				return VectorStoreObservationAttributes.DB_SYSTEM.value();
			}
		}

	}

	public enum HighCardinalityKeyNames implements KeyName {

		DB_COLLECTION_NAME {
			@Override
			public String asString() {
				return VectorStoreObservationAttributes.DB_COLLECTION_NAME.value();
			}
		},

		DB_NAMESPACE {
			@Override
			public String asString() {
				return VectorStoreObservationAttributes.DB_NAMESPACE.value();
			}
		},

		DB_SEARCH_SIMILARITY_METRIC {
			@Override
			public String asString() {
				return VectorStoreObservationAttributes.DB_SEARCH_SIMILARITY_METRIC.value();
			}
		},

		DB_VECTOR_DIMENSION_COUNT {
			@Override
			public String asString() {
				return VectorStoreObservationAttributes.DB_VECTOR_DIMENSION_COUNT.value();
			}
		},

		DB_VECTOR_FIELD_NAME {
			@Override
			public String asString() {
				return VectorStoreObservationAttributes.DB_VECTOR_FIELD_NAME.value();
			}
		},

		DB_VECTOR_QUERY_CONTENT {
			@Override
			public String asString() {
				return VectorStoreObservationAttributes.DB_VECTOR_QUERY_CONTENT.value();
			}
		},

		DB_VECTOR_QUERY_FILTER {
			@Override
			public String asString() {
				return "db.vector.query.filter";
			}
		},

		DB_VECTOR_QUERY_RESPONSE_DOCUMENTS {
			@Override
			public String asString() {
				return "db.vector.query.response.documents";
			}
		},

		DB_VECTOR_QUERY_SIMILARITY_THRESHOLD {
			@Override
			public String asString() {
				return VectorStoreObservationAttributes.DB_VECTOR_QUERY_SIMILARITY_THRESHOLD.value();
			}
		},

		DB_VECTOR_QUERY_TOP_K {
			@Override
			public String asString() {
				return VectorStoreObservationAttributes.DB_VECTOR_QUERY_TOP_K.value();
			}
		}

	}

}
