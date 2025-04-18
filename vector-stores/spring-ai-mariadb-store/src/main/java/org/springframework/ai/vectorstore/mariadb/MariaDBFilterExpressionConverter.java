package org.springframework.ai.vectorstore.mariadb;

import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.Filter.Group;
import org.springframework.ai.vectorstore.filter.Filter.Key;
import org.springframework.ai.vectorstore.filter.converter.AbstractFilterExpressionConverter;

public class MariaDBFilterExpressionConverter extends AbstractFilterExpressionConverter {

	private final String metadataFieldName;

	public MariaDBFilterExpressionConverter(String metadataFieldName) {
		this.metadataFieldName = metadataFieldName;
	}

	@Override
	protected void doExpression(Expression expression, StringBuilder context) {
		this.convertOperand(expression.left(), context);
		context.append(getOperationSymbol(expression));
		this.convertOperand(expression.right(), context);
	}

	@Override
	protected void doSingleValue(Object value, StringBuilder context) {
		if (value instanceof String) {
			context.append(String.format("\'%s\'", value));
		}
		else {
			context.append(value);
		}
	}

	private String getOperationSymbol(Expression exp) {
		return switch (exp.type()) {
			case AND -> " AND ";
			case OR -> " OR ";
			case EQ -> " = ";
			case NE -> " != ";
			case LT -> " < ";
			case LTE -> " <= ";
			case GT -> " > ";
			case GTE -> " >= ";
			case IN -> " IN ";
			case NOT, NIN -> " NOT IN ";

			default -> throw new RuntimeException("Not supported expression type: " + exp.type());
		};
	}

	@Override
	protected void doKey(Key key, StringBuilder context) {
		context.append("JSON_VALUE(" + this.metadataFieldName + ", '$." + key.key() + "')");
	}

	protected void doStartValueRange(Filter.Value listValue, StringBuilder context) {
		context.append("(");
	}

	protected void doEndValueRange(Filter.Value listValue, StringBuilder context) {
		context.append(")");
	}

	@Override
	protected void doStartGroup(Group group, StringBuilder context) {
		context.append("(");
	}

	@Override
	protected void doEndGroup(Group group, StringBuilder context) {
		context.append(")");
	}

}
