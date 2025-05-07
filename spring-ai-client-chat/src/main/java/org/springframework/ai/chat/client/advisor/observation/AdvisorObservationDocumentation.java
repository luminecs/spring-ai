package org.springframework.ai.chat.client.advisor.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

import org.springframework.ai.observation.conventions.AiObservationAttributes;

public enum AdvisorObservationDocumentation implements ObservationDocumentation {

	AI_ADVISOR {
		@Override
		public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
			return DefaultAdvisorObservationConvention.class;
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

		SPRING_AI_KIND {
			@Override
			public String asString() {
				return "spring.ai.kind";
			}
		},

		ADVISOR_NAME {
			@Override
			public String asString() {
				return "spring.ai.advisor.name";
			}
		},

	}

	public enum HighCardinalityKeyNames implements KeyName {

		ADVISOR_ORDER {
			@Override
			public String asString() {
				return "spring.ai.advisor.order";
			}
		}

	}

}
