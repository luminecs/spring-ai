package org.springframework.ai.evaluation;

import java.util.Map;
import java.util.Objects;

public class EvaluationResponse {

	private final boolean pass;

	private final float score;

	private final String feedback;

	private final Map<String, Object> metadata;

	public EvaluationResponse(boolean pass, float score, String feedback, Map<String, Object> metadata) {
		this.pass = pass;
		this.score = score;
		this.feedback = feedback;
		this.metadata = metadata;
	}

	public EvaluationResponse(boolean pass, String feedback, Map<String, Object> metadata) {
		this.pass = pass;
		this.score = 0;
		this.feedback = feedback;
		this.metadata = metadata;
	}

	public boolean isPass() {
		return this.pass;
	}

	public float getScore() {
		return this.score;
	}

	public String getFeedback() {
		return this.feedback;
	}

	public Map<String, Object> getMetadata() {
		return this.metadata;
	}

	@Override
	public String toString() {
		return "EvaluationResponse{" + "pass=" + this.pass + ", score=" + this.score + ", feedback='" + this.feedback
				+ '\'' + ", metadata=" + this.metadata + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof EvaluationResponse that)) {
			return false;
		}
		return this.pass == that.pass && Float.compare(this.score, that.score) == 0
				&& Objects.equals(this.feedback, that.feedback) && Objects.equals(this.metadata, that.metadata);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.pass, this.score, this.feedback, this.metadata);
	}

}
