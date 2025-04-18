package org.springframework.ai.chat.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

import org.springframework.ai.observation.conventions.AiObservationAttributes;
import org.springframework.ai.observation.conventions.AiObservationEventNames;

public enum ChatModelObservationDocumentation implements ObservationDocumentation {

	CHAT_MODEL_OPERATION {
		@Override
		public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
			return DefaultChatModelObservationConvention.class;
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
		},

		RESPONSE_MODEL {
			@Override
			public String asString() {
				return AiObservationAttributes.RESPONSE_MODEL.value();
			}
		}

	}

	public enum HighCardinalityKeyNames implements KeyName {

		REQUEST_FREQUENCY_PENALTY {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_FREQUENCY_PENALTY.value();
			}
		},

		REQUEST_MAX_TOKENS {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_MAX_TOKENS.value();
			}
		},

		REQUEST_PRESENCE_PENALTY {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_PRESENCE_PENALTY.value();
			}
		},

		REQUEST_STOP_SEQUENCES {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_STOP_SEQUENCES.value();
			}
		},

		REQUEST_TEMPERATURE {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_TEMPERATURE.value();
			}
		},

		REQUEST_TOP_K {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_TOP_K.value();
			}
		},

		REQUEST_TOP_P {
			@Override
			public String asString() {
				return AiObservationAttributes.REQUEST_TOP_P.value();
			}
		},

		RESPONSE_FINISH_REASONS {
			@Override
			public String asString() {
				return AiObservationAttributes.RESPONSE_FINISH_REASONS.value();
			}
		},

		RESPONSE_ID {
			@Override
			public String asString() {
				return AiObservationAttributes.RESPONSE_ID.value();
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
		},

		COMPLETION {
			@Override
			public String asString() {
				return AiObservationAttributes.COMPLETION.value();
			}
		}

	}

	public enum Events implements Observation.Event {

		CONTENT_PROMPT {
			@Override
			public String getName() {
				return AiObservationEventNames.CONTENT_PROMPT.value();
			}
		},

		CONTENT_COMPLETION {
			@Override
			public String getName() {
				return AiObservationEventNames.CONTENT_COMPLETION.value();
			}
		}

	}

}
