package org.femto.jaggqlyapp;

import java.util.Map;

import org.femto.jaggqlyapp.aggqly.AggqlyDataLoaders;
import org.femto.jaggqlyapp.aggqly.expressions.JoinEmitter;
import org.femto.jaggqlyapp.aggqly.expressions.Lexer;
import org.femto.jaggqlyapp.aggqly.expressions.Parser;
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
	void contextLoads() {
		var c = ctx.containsBean("aggqlyDataLoaders");
		var x = ctx.getBean("aggqlyDataLoaders", AggqlyDataLoaders.class);

		var e = JoinExpressionImpl.fromString("""
				/{l:right_id} = /{r:id}
				AND /{l:age} < /{arg:$.$.age}
				""");

		var sql = e.method("ltab", "rtab", Map.of("age", ":age_1"), Map.of());

		System.out.println(sql);
	}

	@Test
	void expressionParser() {
		String input = "{!l(id)} = {!arg(id)} {?ctx(context) AND {!l(something)} = {!ctx(context)}}";
		var lexer = new Lexer();
		var tokens = lexer.tokenize(input);
		var nodes = new Parser().parse(new TokenStream(tokens));
		var expression = new JoinEmitter().emit(nodes);

		var x = expression.get("ltab", "rtab", Map.of("id", "1"), Map.of());
		System.out.println(x);
	}

	@Test
	void gqlQueryTest() {
		var r = dgsQueryExecutor.execute("""
					query shows($minRating: Int) {
						shows {
							title,
							reviews(minRating: $minRating) {
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
