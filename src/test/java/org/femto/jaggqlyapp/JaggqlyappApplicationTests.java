package org.femto.jaggqlyapp;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.femto.jaggqlyapp.aggqly.AggqlyDataLoaders;
import org.femto.jaggqlyapp.aggqly.expressions.JoinEmitter;
import org.femto.jaggqlyapp.aggqly.expressions.Lexer;
import org.femto.jaggqlyapp.aggqly.expressions.Parser;
import org.femto.jaggqlyapp.aggqly.expressions.ParserException;
import org.femto.jaggqlyapp.aggqly.expressions.ParserMode;
import org.femto.jaggqlyapp.aggqly.expressions.TokenStream;
import org.femto.jaggqlyapp.aggqly.impl.JoinExpressionImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import com.netflix.graphql.dgs.DgsQueryExecutor;

@SpringBootTest
class JaggqlyappApplicationTests {

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private DgsQueryExecutor dgsQueryExecutor;

	@Test
	void ancestorAccess() {
		String input = "{!t.$.$(id)} = {!arg(id)}";
		var lexer = new Lexer();
		final var tokens1 = lexer.tokenize(input);
		var nodes = new Parser(ParserMode.WHERE_EXPRESSION).parse(new TokenStream(tokens1));

		input = "{!ctx.$.$(id)} = {!arg(id)}";
		lexer = new Lexer();
		final var tokens2 = lexer.tokenize(input);
		assertThrows(ParserException.class,
				() -> new Parser(ParserMode.WHERE_EXPRESSION).parse(new TokenStream(tokens2)));
	}

	@Test
	void expressionParser() {
		String input = "{!t(id)} = {!arg(id)} {?ctx(context) AND {!t(something)} = {!ctx(context)}}";
		var lexer = new Lexer();
		var tokens = lexer.tokenize(input);
		var nodes = new Parser(ParserMode.WHERE_EXPRESSION).parse(new TokenStream(tokens));
		var expression = new JoinEmitter().emit(nodes);

		var x = expression.get("ltab", "rtab", Map.of("id", "1"), Map.of());
		System.out.println(x);
	}

	@Test
	void testJunction() {
		var r = dgsQueryExecutor.execute("""
					query {
						shows {
							actors {
								name
							}
						}
					}
				""");

		System.out.println(r);
	}

	@Test
	void testTableExpression() {
		var r = dgsQueryExecutor.execute("""
					query {
						categories {
							name
						}
					}
				""");

		System.out.println(r);
	}

	@Test
	void gqlQueryTest() {
		var r = dgsQueryExecutor.execute("""
					query shows($minRating: Int) {
						shows(orderBy: { title: ASC, releaseYear: DESC }) {
							title
							releaseYear
							reviews(minRating: $minRating, orderBy: { rating: DESC }) {
								... on ShortReview {
									rating
								}
								... on FullReview {
									rating
									comment
								}
							}
						}
					}
				""", Map.of("minRating", 3));

		System.out.println(r);
	}

	// {?arg:<arg> AND {arg:<arg>}}
	// {?ctx:<ctx> AND {ctx:<ctx>} {?arg:<arg> OR {arg:<arg>} }}
	// SelectNode (Table, (ColumnNode|JoinNode|JunctionNode)[], WhereNode,
	// OrderNode[])
	// JoinNode (Alias, SelectNode)

	// parseJoin(alias, gqlType, aggqlyType, gqlSelectionSet, JoinExpression)

	// parseSelect(gqlType, aggqlyType, gqlSelectionSet, WhereExpression,
	// OrderExpression)
}
