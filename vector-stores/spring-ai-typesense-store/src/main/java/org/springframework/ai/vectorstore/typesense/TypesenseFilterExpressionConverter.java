package org.springframework.ai.vectorstore.typesense;

import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.converter.AbstractFilterExpressionConverter;

public class TypesenseFilterExpressionConverter extends AbstractFilterExpressionConverter {

	@Override
	protected void doExpression(Filter.Expression exp, StringBuilder context) {
		this.convertOperand(exp.left(), context);
		context.append(getOperationSymbol(exp));
		this.convertOperand(exp.right(), context);
	}

	private String getOperationSymbol(Filter.Expression exp) {
		return switch (exp.type()) {
			case AND -> " && ";
			case OR -> " || ";
			case EQ -> " ";
			case NE -> " != ";
			case LT -> " < ";
			case LTE -> " <= ";
			case GT -> " > ";
			case GTE -> " >= ";
			case IN -> " ";
			case NIN -> " != ";

			default -> throw new RuntimeException("Not supported expression type:" + exp.type());
		};
	}

	@Override
	protected void doGroup(Filter.Group group, StringBuilder context) {
		this.convertOperand(new Filter.Expression(Filter.ExpressionType.AND, group.content(), group.content()),
				context);
	}

	@Override
	protected void doKey(Filter.Key key, StringBuilder context) {
		context.append("metadata." + key.key() + ":");
	}

}
