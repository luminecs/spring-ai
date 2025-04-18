package org.springframework.ai.vectorstore.filter;

import java.util.List;

import org.springframework.ai.vectorstore.filter.Filter.ExpressionType;
import org.springframework.ai.vectorstore.filter.Filter.Key;
import org.springframework.ai.vectorstore.filter.Filter.Value;

public class FilterExpressionBuilder {

	public Op eq(String key, Object value) {
		return new Op(new Filter.Expression(ExpressionType.EQ, new Key(key), new Value(value)));
	}

	public Op ne(String key, Object value) {
		return new Op(new Filter.Expression(ExpressionType.NE, new Key(key), new Value(value)));
	}

	public Op gt(String key, Object value) {
		return new Op(new Filter.Expression(ExpressionType.GT, new Key(key), new Value(value)));
	}

	public Op gte(String key, Object value) {
		return new Op(new Filter.Expression(ExpressionType.GTE, new Key(key), new Value(value)));
	}

	public Op lt(String key, Object value) {
		return new Op(new Filter.Expression(ExpressionType.LT, new Key(key), new Value(value)));
	}

	public Op lte(String key, Object value) {
		return new Op(new Filter.Expression(ExpressionType.LTE, new Key(key), new Value(value)));
	}

	public Op and(Op left, Op right) {
		return new Op(new Filter.Expression(ExpressionType.AND, left.expression, right.expression));
	}

	public Op or(Op left, Op right) {
		return new Op(new Filter.Expression(ExpressionType.OR, left.expression, right.expression));
	}

	public Op in(String key, Object... values) {
		return this.in(key, List.of(values));
	}

	public Op in(String key, List<Object> values) {
		return new Op(new Filter.Expression(ExpressionType.IN, new Key(key), new Value(values)));
	}

	public Op nin(String key, Object... values) {
		return this.nin(key, List.of(values));
	}

	public Op nin(String key, List<Object> values) {
		return new Op(new Filter.Expression(ExpressionType.NIN, new Key(key), new Value(values)));
	}

	public Op group(Op content) {
		return new Op(new Filter.Group(content.build()));
	}

	public Op not(Op content) {
		return new Op(new Filter.Expression(ExpressionType.NOT, content.expression, null));
	}

	public record Op(Filter.Operand expression) {

		public Filter.Expression build() {
			if (this.expression instanceof Filter.Group group) {

				return group.content();
			}
			else if (this.expression instanceof Filter.Expression exp) {
				return exp;
			}
			throw new RuntimeException("Invalid expression: " + this.expression);
		}

	}

}
