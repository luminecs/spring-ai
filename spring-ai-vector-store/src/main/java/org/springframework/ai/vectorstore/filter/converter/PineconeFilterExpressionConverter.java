package org.springframework.ai.vectorstore.filter.converter;

import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.Filter.ExpressionType;
import org.springframework.ai.vectorstore.filter.Filter.Key;

public class PineconeFilterExpressionConverter extends AbstractFilterExpressionConverter {

	@Override
	protected void doExpression(Expression exp, StringBuilder context) {

		context.append("{");
		if (exp.type() == ExpressionType.AND || exp.type() == ExpressionType.OR) {
			context.append(getOperationSymbol(exp));
			context.append("[");
			this.convertOperand(exp.left(), context);
			context.append(",");
			this.convertOperand(exp.right(), context);
			context.append("]");
		}
		else {
			this.convertOperand(exp.left(), context);
			context.append("{");
			context.append(getOperationSymbol(exp));
			this.convertOperand(exp.right(), context);
			context.append("}");
		}
		context.append("}");

	}

	private String getOperationSymbol(Expression exp) {
		return "\"$" + exp.type().toString().toLowerCase() + "\": ";
	}

	@Override
	protected void doKey(Key key, StringBuilder context) {
		var identifier = (hasOuterQuotes(key.key())) ? removeOuterQuotes(key.key()) : key.key();
		context.append("\"").append(identifier).append("\": ");
	}

}
