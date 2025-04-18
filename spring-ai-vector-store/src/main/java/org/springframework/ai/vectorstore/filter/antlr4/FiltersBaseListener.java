package org.springframework.ai.vectorstore.filter.antlr4;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

@SuppressWarnings("CheckReturnValue")
public class FiltersBaseListener implements FiltersListener {

	@Override
	public void enterWhere(FiltersParser.WhereContext ctx) {
	}

	@Override
	public void exitWhere(FiltersParser.WhereContext ctx) {
	}

	@Override
	public void enterNinExpression(FiltersParser.NinExpressionContext ctx) {
	}

	@Override
	public void exitNinExpression(FiltersParser.NinExpressionContext ctx) {
	}

	@Override
	public void enterAndExpression(FiltersParser.AndExpressionContext ctx) {
	}

	@Override
	public void exitAndExpression(FiltersParser.AndExpressionContext ctx) {
	}

	@Override
	public void enterInExpression(FiltersParser.InExpressionContext ctx) {
	}

	@Override
	public void exitInExpression(FiltersParser.InExpressionContext ctx) {
	}

	@Override
	public void enterNotExpression(FiltersParser.NotExpressionContext ctx) {
	}

	@Override
	public void exitNotExpression(FiltersParser.NotExpressionContext ctx) {
	}

	@Override
	public void enterCompareExpression(FiltersParser.CompareExpressionContext ctx) {
	}

	@Override
	public void exitCompareExpression(FiltersParser.CompareExpressionContext ctx) {
	}

	@Override
	public void enterOrExpression(FiltersParser.OrExpressionContext ctx) {
	}

	@Override
	public void exitOrExpression(FiltersParser.OrExpressionContext ctx) {
	}

	@Override
	public void enterGroupExpression(FiltersParser.GroupExpressionContext ctx) {
	}

	@Override
	public void exitGroupExpression(FiltersParser.GroupExpressionContext ctx) {
	}

	@Override
	public void enterConstantArray(FiltersParser.ConstantArrayContext ctx) {
	}

	@Override
	public void exitConstantArray(FiltersParser.ConstantArrayContext ctx) {
	}

	@Override
	public void enterCompare(FiltersParser.CompareContext ctx) {
	}

	@Override
	public void exitCompare(FiltersParser.CompareContext ctx) {
	}

	@Override
	public void enterIdentifier(FiltersParser.IdentifierContext ctx) {
	}

	@Override
	public void exitIdentifier(FiltersParser.IdentifierContext ctx) {
	}

	@Override
	public void enterIntegerConstant(FiltersParser.IntegerConstantContext ctx) {
	}

	@Override
	public void exitIntegerConstant(FiltersParser.IntegerConstantContext ctx) {
	}

	@Override
	public void enterDecimalConstant(FiltersParser.DecimalConstantContext ctx) {
	}

	@Override
	public void exitDecimalConstant(FiltersParser.DecimalConstantContext ctx) {
	}

	@Override
	public void enterTextConstant(FiltersParser.TextConstantContext ctx) {
	}

	@Override
	public void exitTextConstant(FiltersParser.TextConstantContext ctx) {
	}

	@Override
	public void enterBooleanConstant(FiltersParser.BooleanConstantContext ctx) {
	}

	@Override
	public void exitBooleanConstant(FiltersParser.BooleanConstantContext ctx) {
	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
	}

	@Override
	public void visitTerminal(TerminalNode node) {
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
	}

}
