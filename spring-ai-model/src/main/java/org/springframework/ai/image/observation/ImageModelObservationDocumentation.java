package org.springframework.ai.image.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

import org.springframework.ai.observation.conventions.AiObservationAttributes;
import org.springframework.ai.observation.conventions.AiObservationEventNames;

public enum ImageModelObservationDocumentation implements ObservationDocumentation {

	IMAGE_MODEL_OPERATION {
		@Override
		public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
			return DefaultImageModelObservationConvention.class;
		}

		@Override
		public KeyName[] getLowCardinalityKeyNames() {
			return LowCardinalityKeyNames.values();
		}

		@Override
		public KeyName[] getHighCardinalityKeyNames() {
			return HighCardinalityKeyNames.values();
		}

		@Override
		public Observation.Event[] getEvents() {
			return Events.values();
		}
	};

	public enum LowCardinalityKeyNames implements KeyName {

		AI_OPERATION_TYPE {
			@Override
			public String asString() {
				return AiObservationAttributes.AI_OPERATION_TYPE.value();
			}
		},

		AI_PROVIDER {
			@Override
			public String asString() {
				return AiObservationAttributes.AI_PROVIDER.value();
			}
		},

		REQUEST_MODEL {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_MODEL.value();
			}
		}

	}

	public enum HighCardinalityKeyNames implements KeyName {

		REQUEST_IMAGE_RESPONSE_FORMAT {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_IMAGE_RESPONSE_FORMAT.value();
			}
		},

		REQUEST_IMAGE_SIZE {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_IMAGE_SIZE.value();
			}
		},

		REQUEST_IMAGE_STYLE {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_IMAGE_STYLE.value();
			}
		},

		RESPONSE_ID {
			@Override
			public String asString() {
				return AiObservationAttributes.RESPONSE_ID.value();
			}
		},

		RESPONSE_MODEL {
			@Override
			public String asString() {
				return AiObservationAttributes.RESPONSE_MODEL.value();
			}
		},

		USAGE_INPUT_TOKENS {
			@Override
			public String asString() {
				return AiObservationAttributes.USAGE_INPUT_TOKENS.value();
			}
		},

		USAGE_OUTPUT_TOKENS {
			@Override
			public String asString() {
				return AiObservationAttributes.USAGE_OUTPUT_TOKENS.value();
			}
		},

		USAGE_TOTAL_TOKENS {
			@Override
			public String asString() {
				return AiObservationAttributes.USAGE_TOTAL_TOKENS.value();
			}
		},

		PROMPT {
			@Override
			public String asString() {
				return AiObservationAttributes.PROMPT.value();
			}
		}

	}

	public enum Events implements Observation.Event {

		CONTENT_PROMPT {
			@Override
			public String getName() {
				return AiObservationEventNames.CONTENT_PROMPT.value();
			}
		}

	}

}
