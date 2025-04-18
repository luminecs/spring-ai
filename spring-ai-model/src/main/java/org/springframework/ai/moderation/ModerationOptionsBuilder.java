package org.springframework.ai.moderation;

public final class ModerationOptionsBuilder {

	private final ModerationModelOptionsImpl options = new ModerationModelOptionsImpl();

	private ModerationOptionsBuilder() {

	}

	public static ModerationOptionsBuilder builder() {
		return new ModerationOptionsBuilder();
	}

	public ModerationOptionsBuilder model(String model) {
		this.options.setModel(model);
		return this;
	}

	public ModerationOptions build() {
		return this.options;
	}

	private class ModerationModelOptionsImpl implements ModerationOptions {

		private String model;

		@Override
		public String getModel() {
			return this.model;
		}

		public void setModel(String model) {
			this.model = model;
		}

	}

}
