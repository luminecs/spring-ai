package org.springframework.ai.vectorstore.neo4j.filter;

import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.Filter.Group;
import org.springframework.ai.vectorstore.filter.Filter.Key;
import org.springframework.ai.vectorstore.filter.converter.AbstractFilterExpressionConverter;

public class Neo4jVectorFilterExpressionConverter extends AbstractFilterExpressionConverter {

	@Override
	protected void doExpression(Expression expression, StringBuilder context) {
		if (expression.type() == Filter.ExpressionType.NIN) {

			this.doNot(new Expression(Filter.ExpressionType.NOT,
					new Expression(Filter.ExpressionType.IN, expression.left(), expression.right())), context);
		}
		else {
			this.convertOperand(expression.left(), context);
			context.append(this.getOperationSymbol(expression));
			this.convertOperand(expression.right(), context);
		}
	}

	private String getOperationSymbol(Expression exp) {
		return switch (exp.type()) {
			case AND -> " AND ";
			case OR -> " OR ";
			case EQ -> " = ";
			case NE -> " <> ";
			case LT -> " < ";
			case LTE -> " <= ";
			case GT -> " > ";
			case GTE -> " >= ";
			case IN -> " IN ";
			case NOT, NIN -> " NOT ";

			default -> throw new RuntimeException("Not supported expression type: " + exp.type());
		};
	}

	@Override
	protected void doNot(Expression expression, StringBuilder context) {
		Filter.ExpressionType expressionType = expression.type();

		if (expressionType != Filter.ExpressionType.NOT) {
			throw new RuntimeException(
					"Unsupported expression type %s. Only NOT is supported here".formatted(expressionType));
		}

		context.append("NOT ").append(this.convertOperand(expression.left()));
	}

	@Override
	protected void doKey(Key key, StringBuilder context) {
		context.append("node.").append("`metadata.").append(key.key().replace("\"", "")).append("`");
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
