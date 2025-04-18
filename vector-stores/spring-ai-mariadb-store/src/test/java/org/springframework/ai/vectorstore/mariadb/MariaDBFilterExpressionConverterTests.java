package org.springframework.ai.vectorstore.mariadb;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.Filter.Group;
import org.springframework.ai.vectorstore.filter.Filter.Key;
import org.springframework.ai.vectorstore.filter.Filter.Value;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.AND;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.EQ;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.GTE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.IN;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.LTE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.NE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.NIN;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.OR;

public class MariaDBFilterExpressionConverterTests {

	FilterExpressionConverter converter = new MariaDBFilterExpressionConverter("metadata");

	@Test
	public void testEQ() {

		String vectorExpr = this.converter.convertExpression(new Expression(EQ, new Key("country"), new Value("BG")));
		assertThat(vectorExpr).isEqualTo("JSON_VALUE(metadata, '$.country') = 'BG'");
	}

	@Test
	public void tesEqAndGte() {

		String vectorExpr = this.converter
			.convertExpression(new Expression(AND, new Expression(EQ, new Key("genre"), new Value("drama")),
					new Expression(GTE, new Key("year"), new Value(2020))));
		assertThat(vectorExpr)
			.isEqualTo("JSON_VALUE(metadata, '$.genre') = 'drama' AND JSON_VALUE(metadata, '$.year') >=" + " 2020");
	}

	@Test
	public void tesIn() {

		String vectorExpr = this.converter.convertExpression(
				new Expression(IN, new Key("genre"), new Value(List.of("comedy", "documentary", "drama"))));
		assertThat(vectorExpr).isEqualTo("JSON_VALUE(metadata, '$.genre') IN ('comedy','documentary','drama')");
	}

	@Test
	public void testNe() {

		String vectorExpr = this.converter
			.convertExpression(new Expression(OR, new Expression(GTE, new Key("year"), new Value(2020)),
					new Expression(AND, new Expression(EQ, new Key("country"), new Value("BG")),
							new Expression(NE, new Key("city"), new Value("Sofia")))));
		assertThat(vectorExpr)
			.isEqualTo("JSON_VALUE(metadata, '$.year') >= 2020 OR JSON_VALUE(metadata, '$.country') = 'BG'"
					+ " AND JSON_VALUE(metadata, '$.city') != 'Sofia'");
	}

	@Test
	public void testGroup() {

		String vectorExpr = this.converter.convertExpression(new Expression(AND,
				new Group(new Expression(OR, new Expression(GTE, new Key("year"), new Value(2020)),
						new Expression(EQ, new Key("country"), new Value("BG")))),
				new Expression(NIN, new Key("city"), new Value(List.of("Sofia", "Plovdiv")))));
		assertThat(vectorExpr)
			.isEqualTo("(JSON_VALUE(metadata, '$.year') >= 2020 OR JSON_VALUE(metadata, '$.country') ="
					+ " 'BG') AND JSON_VALUE(metadata, '$.city') NOT IN ('Sofia','Plovdiv')");
	}

	@Test
	public void testBoolean() {

		String vectorExpr = this.converter.convertExpression(new Expression(AND,
				new Expression(AND, new Expression(EQ, new Key("isOpen"), new Value(true)),
						new Expression(GTE, new Key("year"), new Value(2020))),
				new Expression(IN, new Key("country"), new Value(List.of("BG", "NL", "US")))));

		assertThat(vectorExpr)
			.isEqualTo("JSON_VALUE(metadata, '$.isOpen') = true AND JSON_VALUE(metadata, '$.year') >= 2020"
					+ " AND JSON_VALUE(metadata, '$.country') IN ('BG','NL','US')");
	}

	@Test
	public void testDecimal() {

		String vectorExpr = this.converter
			.convertExpression(new Expression(AND, new Expression(GTE, new Key("temperature"), new Value(-15.6)),
					new Expression(LTE, new Key("temperature"), new Value(20.13))));

		assertThat(vectorExpr).isEqualTo("JSON_VALUE(metadata, '$.temperature') >= -15.6 AND JSON_VALUE(metadata,"
				+ " '$.temperature') <= 20.13");
	}

	@Test
	public void testComplexIdentifiers() {
		String vectorExpr = this.converter
			.convertExpression(new Expression(EQ, new Key("\"country 1 2 3\""), new Value("BG")));
		assertThat(vectorExpr).isEqualTo("JSON_VALUE(metadata, '$.\"country 1 2 3\"') = 'BG'");
	}

}
