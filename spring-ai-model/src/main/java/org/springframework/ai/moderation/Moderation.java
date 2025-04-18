package org.springframework.ai.moderation;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class Moderation {

	private final String id;

	private final String model;

	private final List<ModerationResult> results;

	private Moderation(Builder builder) {
		this.id = builder.id;
		this.model = builder.model;
		this.results = builder.moderationResultList;
	}

	public static Builder builder() {
		return new Builder();
	}

	public String getId() {
		return this.id;
	}

	public String getModel() {
		return this.model;
	}

	public List<ModerationResult> getResults() {
		return this.results;
	}

	@Override
	public String toString() {
		return "Moderation{" + "id='" + this.id + '\'' + ", model='" + this.model + '\'' + ", results="
				+ Arrays.toString(this.results.toArray()) + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Moderation)) {
			return false;
		}
		Moderation that = (Moderation) o;
		return Objects.equals(this.id, that.id) && Objects.equals(this.model, that.model)
				&& Objects.equals(this.results, that.results);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.model, this.results);
	}

	public static class Builder {

		private String id;

		private String model;

		private List<ModerationResult> moderationResultList;

		public Builder id(String id) {
			this.id = id;
			return this;
		}

		public Builder model(String model) {
			this.model = model;
			return this;
		}

		public Builder results(List<ModerationResult> results) {
			this.moderationResultList = results;
			return this;
		}

		public Moderation build() {
			return new Moderation(this);
		}

	}

}
