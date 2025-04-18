package org.springframework.ai.vectorstore.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.Filter.ExpressionType;
import org.springframework.ai.vectorstore.filter.Filter.Operand;
import org.springframework.util.Assert;

public final class FilterHelper {

	private static final Map<ExpressionType, ExpressionType> TYPE_NEGATION_MAP = Map.of(ExpressionType.AND,
			ExpressionType.OR, ExpressionType.OR, ExpressionType.AND, ExpressionType.EQ, ExpressionType.NE,
			ExpressionType.NE, ExpressionType.EQ, ExpressionType.GT, ExpressionType.LTE, ExpressionType.GTE,
			ExpressionType.LT, ExpressionType.LT, ExpressionType.GTE, ExpressionType.LTE, ExpressionType.GT,
			ExpressionType.IN, ExpressionType.NIN, ExpressionType.NIN, ExpressionType.IN);

	private FilterHelper() {
	}

	public static Filter.Operand negate(Filter.Operand operand) {

		if (operand instanceof Filter.Group group) {
			Operand inEx = negate(group.content());
			if (inEx instanceof Filter.Group inEx2) {
				inEx = inEx2.content();
			}
			return new Filter.Group((Expression) inEx);
		}
		else if (operand instanceof Filter.Expression exp) {
			switch (exp.type()) {
				case NOT:
					return negate(exp.left());
				case AND:
				case OR:
					return new Filter.Expression(TYPE_NEGATION_MAP.get(exp.type()), negate(exp.left()),
							negate(exp.right()));
				case EQ:
				case NE:
				case GT:
				case GTE:
				case LT:
				case LTE:
					return new Filter.Expression(TYPE_NEGATION_MAP.get(exp.type()), exp.left(), exp.right());
				case IN:
				case NIN:
					return new Filter.Expression(TYPE_NEGATION_MAP.get(exp.type()), exp.left(), exp.right());
				default:
					throw new IllegalArgumentException("Unknown expression type: " + exp.type());
			}
		}
		else {
			throw new IllegalArgumentException("Can not negate operand of type: " + operand.getClass());
		}
	}

	public static void expandIn(Expression exp, StringBuilder context,
			FilterExpressionConverter filterExpressionConverter) {
		Assert.isTrue(exp.type() == ExpressionType.IN, "Expected IN expressions but was: " + exp.type());
		expandInNinExpressions(ExpressionType.OR, ExpressionType.EQ, exp, context, filterExpressionConverter);
	}

	public static void expandNin(Expression exp, StringBuilder context,
			FilterExpressionConverter filterExpressionConverter) {
		Assert.isTrue(exp.type() == ExpressionType.NIN, "Expected NIN expressions but was: " + exp.type());
		expandInNinExpressions(ExpressionType.AND, ExpressionType.NE, exp, context, filterExpressionConverter);
	}

	private static void expandInNinExpressions(Filter.ExpressionType outerExpressionType,
			Filter.ExpressionType innerExpressionType, Expression exp, StringBuilder context,
			FilterExpressionConverter expressionConverter) {
		if (exp.right() instanceof Filter.Value value) {
			if (value.value() instanceof List list) {

				List<Filter.Expression> eqExprs = new ArrayList<>();
				for (Object o : list) {
					eqExprs.add(new Filter.Expression(innerExpressionType, exp.left(), new Filter.Value(o)));
				}
				context.append(expressionConverter.convertExpression(aggregate(outerExpressionType, eqExprs)));
			}
			else {

				context.append(expressionConverter
					.convertExpression(new Filter.Expression(innerExpressionType, exp.left(), exp.right())));
			}
		}
		else {
			throw new IllegalStateException(
					"Filter IN right expression should be of Filter.Value type but was " + exp.right().getClass());
		}
	}

	private static Filter.Expression aggregate(Filter.ExpressionType aggregateType,
			List<Filter.Expression> expressions) {

		if (expressions.size() == 1) {
			return expressions.get(0);
		}
		return new Filter.Expression(aggregateType, expressions.get(0),
				aggregate(aggregateType, expressions.subList(1, expressions.size())));
	}

}
