package org.springframework.ai.evaluation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.ai.document.Document;

public class EvaluationRequest {

	private final String userText;

	private final List<Document> dataList;

	private final String responseContent;

	public EvaluationRequest(String userText, String responseContent) {
		this(userText, Collections.emptyList(), responseContent);
	}

	public EvaluationRequest(List<Document> dataList, String responseContent) {
		this("", dataList, responseContent);
	}

	public EvaluationRequest(String userText, List<Document> dataList, String responseContent) {
		this.userText = userText;
		this.dataList = dataList;
		this.responseContent = responseContent;
	}

	public String getUserText() {
		return this.userText;
	}

	public List<Document> getDataList() {
		return this.dataList;
	}

	public String getResponseContent() {
		return this.responseContent;
	}

	@Override
	public String toString() {
		return "EvaluationRequest{" + "userText='" + this.userText + '\'' + ", dataList=" + this.dataList
				+ ", chatResponse=" + this.responseContent + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof EvaluationRequest that)) {
			return false;
		}
		return Objects.equals(this.userText, that.userText) && Objects.equals(this.dataList, that.dataList)
				&& Objects.equals(this.responseContent, that.responseContent);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.userText, this.dataList, this.responseContent);
	}

}
