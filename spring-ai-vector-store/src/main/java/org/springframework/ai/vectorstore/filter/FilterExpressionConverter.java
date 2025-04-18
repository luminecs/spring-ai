package org.springframework.ai.vectorstore.filter;

public interface FilterExpressionConverter {

	String convertExpression(Filter.Expression expression);

}
