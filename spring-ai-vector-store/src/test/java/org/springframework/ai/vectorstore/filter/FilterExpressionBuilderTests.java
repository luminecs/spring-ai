package org.springframework.ai.vectorstore.filter;

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.ai.vectorstore.filter.Filter.Expression;
import org.springframework.ai.vectorstore.filter.Filter.Group;
import org.springframework.ai.vectorstore.filter.Filter.Key;
import org.springframework.ai.vectorstore.filter.Filter.Value;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.AND;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.EQ;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.GTE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.IN;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.NE;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.NIN;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.NOT;
import static org.springframework.ai.vectorstore.filter.Filter.ExpressionType.OR;

public class FilterExpressionBuilderTests {

	FilterExpressionBuilder b = new FilterExpressionBuilder();

	@Test
	public void testEQ() {

		assertThat(this.b.eq("country", "BG").build())
			.isEqualTo(new Expression(EQ, new Key("country"), new Value("BG")));
	}

	@Test
	public void tesEqAndGte() {

		Expression exp = this.b.and(this.b.eq("genre", "drama"), this.b.gte("year", 2020)).build();
		assertThat(exp).isEqualTo(new Expression(AND, new Expression(EQ, new Key("genre"), new Value("drama")),
				new Expression(GTE, new Key("year"), new Value(2020))));
	}

	@Test
	public void testIn() {

		var exp = this.b.in("genre", "comedy", "documentary", "drama").build();
		assertThat(exp)
			.isEqualTo(new Expression(IN, new Key("genre"), new Value(List.of("comedy", "documentary", "drama"))));
	}

	@Test
	public void testNe() {

		var exp = this.b
			.and(this.b.or(this.b.gte("year", 2020), this.b.eq("country", "BG")), this.b.ne("city", "Sofia"))
			.build();

		assertThat(exp).isEqualTo(new Expression(AND,
				new Expression(OR, new Expression(GTE, new Key("year"), new Value(2020)),
						new Expression(EQ, new Key("country"), new Value("BG"))),
				new Expression(NE, new Key("city"), new Value("Sofia"))));
	}

	@Test
	public void testGroup() {

		var exp = this.b
			.and(this.b.group(this.b.or(this.b.gte("year", 2020), this.b.eq("country", "BG"))),
					this.b.nin("city", "Sofia", "Plovdiv"))
			.build();

		assertThat(exp).isEqualTo(new Expression(AND,
				new Group(new Expression(OR, new Expression(GTE, new Key("year"), new Value(2020)),
						new Expression(EQ, new Key("country"), new Value("BG")))),
				new Expression(NIN, new Key("city"), new Value(List.of("Sofia", "Plovdiv")))));
	}

	@Test
	public void tesIn2() {

		var exp = this.b
			.and(this.b.and(this.b.eq("isOpen", true), this.b.gte("year", 2020)),
					this.b.in("country", "BG", "NL", "US"))
			.build();

		assertThat(exp).isEqualTo(new Expression(AND,
				new Expression(AND, new Expression(EQ, new Key("isOpen"), new Value(true)),
						new Expression(GTE, new Key("year"), new Value(2020))),
				new Expression(IN, new Key("country"), new Value(List.of("BG", "NL", "US")))));
	}

	@Test
	public void tesNot() {

		var exp = this.b.not(this.b.and(this.b.and(this.b.eq("isOpen", true), this.b.gte("year", 2020)),
				this.b.in("country", "BG", "NL", "US")))
			.build();

		assertThat(exp).isEqualTo(new Expression(NOT,
				new Expression(AND,
						new Expression(AND, new Expression(EQ, new Key("isOpen"), new Value(true)),
								new Expression(GTE, new Key("year"), new Value(2020))),
						new Expression(IN, new Key("country"), new Value(List.of("BG", "NL", "US")))),
				null));
	}

}
