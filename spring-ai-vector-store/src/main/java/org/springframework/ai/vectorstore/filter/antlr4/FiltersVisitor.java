package org.springframework.ai.vectorstore.filter.antlr4;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

public interface FiltersVisitor<T> extends ParseTreeVisitor<T> {

	T visitWhere(FiltersParser.WhereContext ctx);

	T visitNinExpression(FiltersParser.NinExpressionContext ctx);

	T visitAndExpression(FiltersParser.AndExpressionContext ctx);

	T visitInExpression(FiltersParser.InExpressionContext ctx);

	T visitNotExpression(FiltersParser.NotExpressionContext ctx);

	T visitCompareExpression(FiltersParser.CompareExpressionContext ctx);

	T visitOrExpression(FiltersParser.OrExpressionContext ctx);

	T visitGroupExpression(FiltersParser.GroupExpressionContext ctx);

	T visitConstantArray(FiltersParser.ConstantArrayContext ctx);

	T visitCompare(FiltersParser.CompareContext ctx);

	T visitIdentifier(FiltersParser.IdentifierContext ctx);

	T visitIntegerConstant(FiltersParser.IntegerConstantContext ctx);

	T visitDecimalConstant(FiltersParser.DecimalConstantContext ctx);

	T visitTextConstant(FiltersParser.TextConstantContext ctx);

	T visitBooleanConstant(FiltersParser.BooleanConstantContext ctx);

}
