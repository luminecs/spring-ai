package org.springframework.ai.vectorstore.filter;

public class Filter {

	public enum ExpressionType {

		AND, OR, EQ, NE, GT, GTE, LT, LTE, IN, NIN, NOT

	}

	public interface Operand {

	}

	public record Key(String key) implements Operand {

	}

	public record Value(Object value) implements Operand {

	}

	public record Expression(ExpressionType type, Operand left, Operand right) implements Operand {

		public Expression(ExpressionType type, Operand operand) {
			this(type, operand, null);
		}

	}

	public record Group(Expression content) implements Operand {

	}

}
