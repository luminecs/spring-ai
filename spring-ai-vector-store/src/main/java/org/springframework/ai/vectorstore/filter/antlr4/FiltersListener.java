package org.springframework.ai.vectorstore.filter.antlr4;

import org.antlr.v4.runtime.tree.ParseTreeListener;

public interface FiltersListener extends ParseTreeListener {

	void enterWhere(FiltersParser.WhereContext ctx);

	void exitWhere(FiltersParser.WhereContext ctx);

	void enterNinExpression(FiltersParser.NinExpressionContext ctx);

	void exitNinExpression(FiltersParser.NinExpressionContext ctx);

	void enterAndExpression(FiltersParser.AndExpressionContext ctx);

	void exitAndExpression(FiltersParser.AndExpressionContext ctx);

	void enterInExpression(FiltersParser.InExpressionContext ctx);

	void exitInExpression(FiltersParser.InExpressionContext ctx);

	void enterNotExpression(FiltersParser.NotExpressionContext ctx);

	void exitNotExpression(FiltersParser.NotExpressionContext ctx);

	void enterCompareExpression(FiltersParser.CompareExpressionContext ctx);

	void exitCompareExpression(FiltersParser.CompareExpressionContext ctx);

	void enterOrExpression(FiltersParser.OrExpressionContext ctx);

	void exitOrExpression(FiltersParser.OrExpressionContext ctx);

	void enterGroupExpression(FiltersParser.GroupExpressionContext ctx);

	void exitGroupExpression(FiltersParser.GroupExpressionContext ctx);

	void enterConstantArray(FiltersParser.ConstantArrayContext ctx);

	void exitConstantArray(FiltersParser.ConstantArrayContext ctx);

	void enterCompare(FiltersParser.CompareContext ctx);

	void exitCompare(FiltersParser.CompareContext ctx);

	void enterIdentifier(FiltersParser.IdentifierContext ctx);

	void exitIdentifier(FiltersParser.IdentifierContext ctx);

	void enterIntegerConstant(FiltersParser.IntegerConstantContext ctx);

	void exitIntegerConstant(FiltersParser.IntegerConstantContext ctx);

	void enterDecimalConstant(FiltersParser.DecimalConstantContext ctx);

	void exitDecimalConstant(FiltersParser.DecimalConstantContext ctx);

	void enterTextConstant(FiltersParser.TextConstantContext ctx);

	void exitTextConstant(FiltersParser.TextConstantContext ctx);

	void enterBooleanConstant(FiltersParser.BooleanConstantContext ctx);

	void exitBooleanConstant(FiltersParser.BooleanConstantContext ctx);

}
