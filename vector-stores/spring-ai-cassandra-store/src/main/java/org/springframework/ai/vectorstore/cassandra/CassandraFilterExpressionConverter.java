package org.springframework.ai.vectorstore.cassandra;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.core.type.codec.registry.CodecRegistry;
import com.datastax.oss.driver.shaded.guava.common.base.Preconditions;

import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.Filter.ExpressionType;
import org.springframework.ai.vectorstore.filter.Filter.Key;
import org.springframework.ai.vectorstore.filter.Filter.Value;
import org.springframework.ai.vectorstore.filter.converter.AbstractFilterExpressionConverter;

class CassandraFilterExpressionConverter extends AbstractFilterExpressionConverter {

	private final Map<String, ColumnMetadata> columnsByName;

	CassandraFilterExpressionConverter(Collection<ColumnMetadata> columns) {

		this.columnsByName = columns.stream()
			.collect(Collectors.toMap(c -> c.getName().asInternal(), Function.identity()));
	}

	private static void doOperand(ExpressionType type, StringBuilder context) {
		switch (type) {
			case EQ -> context.append(" = ");
			case NE -> context.append(" != ");
			case GT -> context.append(" > ");
			case GTE -> context.append(" >= ");
			case IN -> context.append(" IN ");
			case LT -> context.append(" < ");
			case LTE -> context.append(" <= ");

			default -> throw new UnsupportedOperationException(
					String.format("Expression type %s not yet implemented. Patches welcome.", type));
		}
	}

	@Override
	protected void doKey(Key key, StringBuilder context) {
		String keyName = key.key();
		Optional<ColumnMetadata> column = getColumn(keyName);
		Preconditions.checkArgument(column.isPresent(), "No metafield %s has been configured", keyName);
		context.append(column.get().getName().asCql(false));
	}

	@Override
	protected void doExpression(Filter.Expression expression, StringBuilder context) {
		switch (expression.type()) {
			case AND -> doBinaryOperation(" and ", expression, context);
			case OR -> doBinaryOperation(" or ", expression, context);
			case NIN, NOT -> throw new UnsupportedOperationException(
					String.format("Expression type %s not yet implemented. Patches welcome.", expression.type()));
			default -> doField(expression, context);
		}
	}

	private void doBinaryOperation(String operator, Filter.Expression expression, StringBuilder context) {
		this.convertOperand(expression.left(), context);
		context.append(operator);
		this.convertOperand(expression.right(), context);
	}

	private void doField(Filter.Expression expression, StringBuilder context) {
		doKey((Key) expression.left(), context);
		doOperand(expression.type(), context);
		ColumnMetadata column = getColumn(((Key) expression.left()).key()).get();
		var v = ((Value) expression.right()).value();
		if (ExpressionType.IN.equals(expression.type())) {
			Preconditions.checkArgument(v instanceof Collection);
			doListValue(column, v, context);
		}
		else {
			doValue(column, v, context);
		}
	}

	private void doListValue(ColumnMetadata column, Object v, StringBuilder context) {
		context.append('(');
		for (var e : (Collection) v) {
			doValue(column, e, context);
			context.append(',');
		}
		context.deleteCharAt(context.length() - 1);
		context.append(')');
	}

	private void doValue(ColumnMetadata column, Object v, StringBuilder context) {
		if (DataTypes.SMALLINT.equals(column.getType())) {
			v = ((Number) v).shortValue();
		}
		context.append(CodecRegistry.DEFAULT.codecFor(column.getType()).format(v));
	}

	private Optional<ColumnMetadata> getColumn(String name) {
		Optional<ColumnMetadata> column = Optional.ofNullable(this.columnsByName.get(name));

		if (column.isEmpty()) {
			if (name.startsWith("\"") && name.endsWith("\"")) {
				name = name.substring(1, name.length() - 1);
				column = Optional.ofNullable(this.columnsByName.get(name));
			}
		}
		return column;
	}

}
