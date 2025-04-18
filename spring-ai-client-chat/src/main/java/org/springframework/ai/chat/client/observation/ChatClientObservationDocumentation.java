package org.springframework.ai.chat.client.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;

public enum ChatClientObservationDocumentation implements ObservationDocumentation {

	AI_CHAT_CLIENT {
		@Override
		public Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
			return DefaultChatClientObservationConvention.class;
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

		STREAM {
			@Override
			public String asString() {
				return "spring.ai.chat.client.stream";
			}
		}

	}

	public enum HighCardinalityKeyNames implements KeyName {

		CHAT_CLIENT_TOOL_FUNCTION_NAMES {
			@Override
			public String asString() {
				return "spring.ai.chat.client.tool.function.names";
			}
		},

		CHAT_CLIENT_TOOL_FUNCTION_CALLBACKS {
			@Override
			public String asString() {
				return "spring.ai.chat.client.tool.function.callbacks";
			}
		},

		CHAT_CLIENT_ADVISORS {
			@Override
			public String asString() {
				return "spring.ai.chat.client.advisors";
			}
		},

		CHAT_CLIENT_ADVISOR_PARAMS {
			@Override
			public String asString() {
				return "spring.ai.chat.client.advisor.params";
			}
		},

		CHAT_CLIENT_USER_TEXT {
			@Override
			public String asString() {
				return "spring.ai.chat.client.user.text";
			}
		},

		CHAT_CLIENT_USER_PARAMS {
			@Override
			public String asString() {
				return "spring.ai.chat.client.user.params";
			}
		},

		CHAT_CLIENT_SYSTEM_TEXT {
			@Override
			public String asString() {
				return "spring.ai.chat.client.system.text";
			}
		},

		CHAT_CLIENT_SYSTEM_PARAM {
			@Override
			public String asString() {
				return "spring.ai.chat.client.system.params";
			}
		}

	}

}
