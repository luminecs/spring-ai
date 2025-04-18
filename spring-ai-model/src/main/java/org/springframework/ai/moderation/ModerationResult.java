package org.springframework.ai.moderation;

import java.util.Objects;

public final class ModerationResult {

	private boolean flagged;

	private Categories categories;

	private CategoryScores categoryScores;

	private ModerationResult(Builder builder) {
		this.flagged = builder.flagged;
		this.categories = builder.categories;
		this.categoryScores = builder.categoryScores;
	}

	public static Builder builder() {
		return new Builder();
	}

	public boolean isFlagged() {
		return this.flagged;
	}

	public void setFlagged(boolean flagged) {
		this.flagged = flagged;
	}

	public Categories getCategories() {
		return this.categories;
	}

	public void setCategories(Categories categories) {
		this.categories = categories;
	}

	public CategoryScores getCategoryScores() {
		return this.categoryScores;
	}

	public void setCategoryScores(CategoryScores categoryScores) {
		this.categoryScores = categoryScores;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof ModerationResult)) {
			return false;
		}
		ModerationResult that = (ModerationResult) o;
		return this.flagged == that.flagged && Objects.equals(this.categories, that.categories)
				&& Objects.equals(this.categoryScores, that.categoryScores);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.flagged, this.categories, this.categoryScores);
	}

	@Override
	public String toString() {
		return "ModerationResult{" + "flagged=" + this.flagged + ", categories=" + this.categories + ", categoryScores="
				+ this.categoryScores + '}';
	}

	public static class Builder {

		private boolean flagged;

		private Categories categories;

		private CategoryScores categoryScores;

		public Builder flagged(boolean flagged) {
			this.flagged = flagged;
			return this;
		}

		public Builder categories(Categories categories) {
			this.categories = categories;
			return this;
		}

		public Builder categoryScores(CategoryScores categoryScores) {
			this.categoryScores = categoryScores;
			return this;
		}

		public ModerationResult build() {
			return new ModerationResult(this);
		}

	}

}
