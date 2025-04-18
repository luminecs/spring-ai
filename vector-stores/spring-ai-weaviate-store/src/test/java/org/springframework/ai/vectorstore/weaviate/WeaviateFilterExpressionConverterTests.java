package org.springframework.ai.vectorstore.weaviate;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.Filter.Group;
import org.springframework.ai.vectorstore.filter.Filter.Key;
import org.springframework.ai.vectorstore.filter.Filter.Value;
import org.springframework.ai.vectorstore.filter.FilterExpressionConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.AND;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.EQ;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.GTE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.IN;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.LTE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.NE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.NIN;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.OR;

public class WeaviateFilterExpressionConverterTests {

	private static String format(String text) {
		return text.trim().replace(" " + System.lineSeparator(), System.lineSeparator()) + System.lineSeparator();
	}

	@Test
	public void testMissingFilterName() {

		FilterExpressionConverter converter = new WeaviateFilterExpressionConverter(List.of());

		assertThatThrownBy(() -> converter.convertExpression(new Expression(EQ, new Key("country"), new Value("BG"))))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(
					"Not allowed filter identifier name: country. Consider adding it to WeaviateVectorStore#filterMetadataKeys.");
	}

	@Test
	public void testSystemIdentifiers() {

		FilterExpressionConverter converter = new WeaviateFilterExpressionConverter(List.of());

		String vectorExpr = converter.convertExpression(new Expression(AND,
				new Expression(AND, new Expression(EQ, new Key("id"), new Value("1")),
						new Expression(GTE, new Key("_creationTimeUnix"), new Value("36"))),
				new Expression(LTE, new Key("_lastUpdateTimeUnix"), new Value("100"))));

		assertThat(format(vectorExpr)).isEqualTo("""
				operator:And
				operands:[{operator:And
				operands:[{path:["id"]
				operator:Equal
				valueText:"1" },
				{path:["_creationTimeUnix"]
				operator:GreaterThanEqual
				valueText:"36" }]},
				{path:["_lastUpdateTimeUnix"]
				operator:LessThanEqual
				valueText:"100" }]
				""");
	}

	@Test
	public void testEQ() {
		FilterExpressionConverter converter = new WeaviateFilterExpressionConverter(List.of("country"));

		String vectorExpr = converter.convertExpression(new Expression(EQ, new Key("country"), new Value("BG")));
		assertThat(format(vectorExpr)).isEqualTo("""
				path:["meta_country"]
				operator:Equal
				valueText:"BG"
				""");
	}

	@Test
	public void tesEqAndGte() {
		FilterExpressionConverter converter = new WeaviateFilterExpressionConverter(List.of("genre", "year"));

		String vectorExpr = converter
			.convertExpression(new Expression(AND, new Expression(EQ, new Key("genre"), new Value("drama")),
					new Expression(GTE, new Key("year"), new Value(2020))));
		assertThat(format(vectorExpr)).isEqualTo("""
				operator:And
				operands:[{path:["meta_genre"]
				operator:Equal
				valueText:"drama" },
				{path:["meta_year"]
				operator:GreaterThanEqual
				valueNumber:2020 }]
				""");
	}

	@Test
	public void tesIn() {
		FilterExpressionConverter converter = new WeaviateFilterExpressionConverter(List.of("genre"));

		String vectorExpr = converter.convertExpression(
				new Expression(IN, new Key("genre"), new Value(List.of("comedy", "documentary", "drama"))));
		assertThat(format(vectorExpr)).isEqualTo("""
				operator:Or
				operands:[{path:["meta_genre"]
				operator:Equal
				valueText:"comedy" },
				{operator:Or
				operands:[{path:["meta_genre"]
				operator:Equal
				valueText:"documentary" },
				{path:["meta_genre"]
				operator:Equal
				valueText:"drama" }]}]
				""");
	}

	@Test
	public void testNe() {
		FilterExpressionConverter converter = new WeaviateFilterExpressionConverter(List.of("city", "year", "country"));

		String vectorExpr = converter
			.convertExpression(new Expression(OR, new Expression(GTE, new Key("year"), new Value(2020)),
					new Expression(AND, new Expression(EQ, new Key("country"), new Value("BG")),
							new Expression(NE, new Key("city"), new Value("Sofia")))));
		assertThat(format(vectorExpr)).isEqualTo("""
				operator:Or
				operands:[{path:["meta_year"]
				operator:GreaterThanEqual
				valueNumber:2020 },
				{operator:And
				operands:[{path:["meta_country"]
				operator:Equal
				valueText:"BG" },
				{path:["meta_city"]
				operator:NotEqual
				valueText:"Sofia" }]}]
				""");
	}

	@Test
	public void testGroup() {
		FilterExpressionConverter converter = new WeaviateFilterExpressionConverter(List.of("city", "year", "country"));

		String vectorExpr = converter.convertExpression(new Expression(AND,
				new Group(new Expression(OR, new Expression(GTE, new Key("year"), new Value(2020)),
						new Expression(EQ, new Key("country"), new Value("BG")))),
				new Expression(NIN, new Key("city"), new Value(List.of("Sofia", "Plovdiv")))));

		assertThat(format(vectorExpr)).isEqualTo("""
				operator:And
				operands:[{operator:And
				operands:[{path:["id"]
				operator:NotEqual
				valueText:"-1" },
				{operator:Or
				operands:[{path:["meta_year"]
				operator:GreaterThanEqual
				valueNumber:2020 },
				{path:["meta_country"]
				operator:Equal
				valueText:"BG" }]}]},
				{operator:And
				operands:[{path:["meta_city"]
				operator:NotEqual
				valueText:"Sofia" },
				{path:["meta_city"]
				operator:NotEqual
				valueText:"Plovdiv" }]}]
				""");
	}

	@Test
	public void tesBoolean() {
		FilterExpressionConverter converter = new WeaviateFilterExpressionConverter(
				List.of("isOpen", "year", "country"));

		String vectorExpr = converter.convertExpression(new Expression(AND,
				new Expression(AND, new Expression(EQ, new Key("isOpen"), new Value(true)),
						new Expression(GTE, new Key("year"), new Value(2020))),
				new Expression(IN, new Key("country"), new Value(List.of("BG", "NL", "US")))));

		assertThat(format(vectorExpr)).isEqualTo("""
				operator:And
				operands:[{operator:And
				operands:[{path:["meta_isOpen"]
				operator:Equal
				valueBoolean:true },
				{path:["meta_year"]
				operator:GreaterThanEqual
				valueNumber:2020 }]},
				{operator:Or
				operands:[{path:["meta_country"]
				operator:Equal
				valueText:"BG" },
				{operator:Or
				operands:[{path:["meta_country"]
				operator:Equal
				valueText:"NL" },
				{path:["meta_country"]
				operator:Equal
				valueText:"US" }]}]}]
				""");
	}

	@Test
	public void testDecimal() {
		FilterExpressionConverter converter = new WeaviateFilterExpressionConverter(List.of("temperature"));

		String vectorExpr = converter
			.convertExpression(new Expression(AND, new Expression(GTE, new Key("temperature"), new Value(-15.6)),
					new Expression(LTE, new Key("temperature"), new Value(20.13))));

		assertThat(format(vectorExpr)).isEqualTo("""
				operator:And
				operands:[{path:["meta_temperature"]
				operator:GreaterThanEqual
				valueNumber:-15.6 },
				{path:["meta_temperature"]
				operator:LessThanEqual
				valueNumber:20.13 }]
				""");
	}

	@Test
	public void testComplexIdentifiers() {
		FilterExpressionConverter converter = new WeaviateFilterExpressionConverter(List.of("country 1 2 3"));

		String vectorExpr = converter
			.convertExpression(new Expression(EQ, new Key("\"country 1 2 3\""), new Value("BG")));
		assertThat(format(vectorExpr)).isEqualTo("""
				path:["meta_country 1 2 3"]
				operator:Equal
				valueText:"BG"
				""");

		vectorExpr = converter.convertExpression(new Expression(EQ, new Key("'country 1 2 3'"), new Value("BG")));
		assertThat(format(vectorExpr)).isEqualTo("""
				path:["meta_country 1 2 3"]
				operator:Equal
				valueText:"BG"
				""");
	}

}
