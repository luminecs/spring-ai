package org.springframework.ai.vectorstore.filter.antlr4;

import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;

@SuppressWarnings("CheckReturnValue")
public class FiltersBaseVisitor<T> extends AbstractParseTreeVisitor<T> implements FiltersVisitor<T> {

	@Override
	public T visitWhere(FiltersParser.WhereContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitNinExpression(FiltersParser.NinExpressionContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitAndExpression(FiltersParser.AndExpressionContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitInExpression(FiltersParser.InExpressionContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitNotExpression(FiltersParser.NotExpressionContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitCompareExpression(FiltersParser.CompareExpressionContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitOrExpression(FiltersParser.OrExpressionContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitGroupExpression(FiltersParser.GroupExpressionContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitConstantArray(FiltersParser.ConstantArrayContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitCompare(FiltersParser.CompareContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitIdentifier(FiltersParser.IdentifierContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitIntegerConstant(FiltersParser.IntegerConstantContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitDecimalConstant(FiltersParser.DecimalConstantContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitTextConstant(FiltersParser.TextConstantContext ctx) {
		return visitChildren(ctx);
	}

	@Override
	public T visitBooleanConstant(FiltersParser.BooleanConstantContext ctx) {
		return visitChildren(ctx);
	}

}
