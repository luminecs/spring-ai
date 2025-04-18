package org.springframework.ai.vectorstore.filter.converter;

import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.Filter.Group;
import org.springframework.ai.vectorstore.filter.Filter.Key;

public class PrintFilterExpressionConverter extends AbstractFilterExpressionConverter {

	public void doExpression(Expression expression, StringBuilder context) {
		this.convertOperand(expression.left(), context);
		context.append(" ").append(expression.type()).append(" ");
		this.convertOperand(expression.right(), context);

	}

	public void doKey(Key key, StringBuilder context) {
		context.append(key.key());
	}

	@Override
	public void doStartGroup(Group group, StringBuilder context) {
		context.append("(");
	}

	@Override
	public void doEndGroup(Group group, StringBuilder context) {
		context.append(")");
	}

}
