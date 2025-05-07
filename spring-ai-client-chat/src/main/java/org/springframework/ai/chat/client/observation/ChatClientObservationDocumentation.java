package org.springframework.ai.chat.client.observation;

import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;
import org.springframework.ai.observation.conventions.AiObservationAttributes;

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

		CHAT_CLIENT_ADVISORS {
			@Override
			public String asString() {
				return "spring.ai.chat.client.advisors";
			}
		},

		CHAT_CLIENT_CONVERSATION_ID {
			@Override
			public String asString() {
				return "spring.ai.chat.client.conversation.id";
			}
		},

		CHAT_CLIENT_TOOL_NAMES {
			@Override
			public String asString() {
				return "spring.ai.chat.client.tool.names";
			}
		},

		PROMPT {
			@Override
			public String asString() {
				return AiObservationAttributes.PROMPT.value();
			}
		},

	}

}
