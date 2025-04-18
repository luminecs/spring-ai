package org.springframework.ai.vectorstore.redis;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.Filter.Group;
import org.springframework.ai.vectorstore.filter.Filter.Key;
import org.springframework.ai.vectorstore.filter.Filter.Value;
import org.springframework.ai.vectorstore.redis.RedisVectorStore.MetadataField;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.AND;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.EQ;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.GTE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.IN;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.LTE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.NE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.NIN;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.OR;

class RedisFilterExpressionConverterTests {

	private static RedisFilterExpressionConverter converter(MetadataField... fields) {
		return new RedisFilterExpressionConverter(Arrays.asList(fields));
	}

	@Test
	void testEQ() {

		String vectorExpr = converter(RedisVectorStore.MetadataField.tag("country"))
			.convertExpression(new Expression(EQ, new Key("country"), new Value("BG")));
		assertThat(vectorExpr).isEqualTo("@country:{BG}");
	}

	@Test
	void tesEqAndGte() {

		String vectorExpr = converter(RedisVectorStore.MetadataField.tag("genre"),
				RedisVectorStore.MetadataField.numeric("year"))
			.convertExpression(new Expression(AND, new Expression(EQ, new Key("genre"), new Value("drama")),
					new Expression(GTE, new Key("year"), new Value(2020))));
		assertThat(vectorExpr).isEqualTo("@genre:{drama} @year:[2020 inf]");
	}

	@Test
	void tesIn() {

		String vectorExpr = converter(RedisVectorStore.MetadataField.tag("genre")).convertExpression(
				new Expression(IN, new Key("genre"), new Value(List.of("comedy", "documentary", "drama"))));
		assertThat(vectorExpr).isEqualTo("@genre:{comedy | documentary | drama}");
	}

	@Test
	void testNe() {

		String vectorExpr = converter(RedisVectorStore.MetadataField.numeric("year"),
				RedisVectorStore.MetadataField.tag("country"), RedisVectorStore.MetadataField.tag("city"))
			.convertExpression(new Expression(OR, new Expression(GTE, new Key("year"), new Value(2020)),
					new Group(new Expression(AND, new Expression(EQ, new Key("country"), new Value("BG")),
							new Expression(NE, new Key("city"), new Value("Sofia"))))));
		assertThat(vectorExpr).isEqualTo("@year:[2020 inf] | (@country:{BG} -@city:{Sofia})");
	}

	@Test
	void testGroup() {

		String vectorExpr = converter(RedisVectorStore.MetadataField.numeric("year"),
				RedisVectorStore.MetadataField.tag("country"), RedisVectorStore.MetadataField.tag("city"))
			.convertExpression(new Expression(AND,
					new Group(new Expression(OR, new Expression(GTE, new Key("year"), new Value(2020)),
							new Expression(EQ, new Key("country"), new Value("BG")))),
					new Expression(NIN, new Key("city"), new Value(List.of("Sofia", "Plovdiv")))));
		assertThat(vectorExpr).isEqualTo("(@year:[2020 inf] | @country:{BG}) -@city:{Sofia | Plovdiv}");
	}

	@Test
	void tesBoolean() {

		String vectorExpr = converter(RedisVectorStore.MetadataField.numeric("year"),
				RedisVectorStore.MetadataField.tag("country"), RedisVectorStore.MetadataField.tag("isOpen"))
			.convertExpression(new Expression(AND,
					new Expression(AND, new Expression(EQ, new Key("isOpen"), new Value(true)),
							new Expression(GTE, new Key("year"), new Value(2020))),
					new Expression(IN, new Key("country"), new Value(List.of("BG", "NL", "US")))));

		assertThat(vectorExpr).isEqualTo("@isOpen:{true} @year:[2020 inf] @country:{BG | NL | US}");
	}

	@Test
	void testDecimal() {

		String vectorExpr = converter(RedisVectorStore.MetadataField.numeric("temperature"))
			.convertExpression(new Expression(AND, new Expression(GTE, new Key("temperature"), new Value(-15.6)),
					new Expression(LTE, new Key("temperature"), new Value(20.13))));

		assertThat(vectorExpr).isEqualTo("@temperature:[-15.6 inf] @temperature:[-inf 20.13]");
	}

	@Test
	void testComplexIdentifiers() {
		String vectorExpr = converter(RedisVectorStore.MetadataField.tag("country 1 2 3"))
			.convertExpression(new Expression(EQ, new Key("\"country 1 2 3\""), new Value("BG")));
		assertThat(vectorExpr).isEqualTo("@\"country 1 2 3\":{BG}");

		vectorExpr = converter(RedisVectorStore.MetadataField.tag("country 1 2 3"))
			.convertExpression(new Expression(EQ, new Key("'country 1 2 3'"), new Value("BG")));
		assertThat(vectorExpr).isEqualTo("@'country 1 2 3':{BG}");
	}

}
