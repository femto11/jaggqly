package org.femto.jaggqlyapp;

import java.util.Map;

import org.femto.jaggqlyapp.aggqly.AggqlyDataLoaders;
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

		var sql = """
				|	SELECT shows_0.title, reviews
				|	FROM shows_0
				|	|	OUTER APPLY (
				|	|	|	SELECT reviews_1.text
				|	|	|	FROM reviews_1
				|	|	|	WHERE shows_0.review_id = reviews_1.id
				|	|	|	  AND (reviews_1.stars >= :minStars_1 OR ... AND ...) <- join expression reviews(minStars)
				|	|	|	ORDER BY x, y, z
				|	|	) FOR JSON PATH reviews
				|	WHERE shows_0.title = :title_0 <- where statement shows(title)
					""";

		System.out.println(r);
	}

	// SelectNode (Table, (ColumnNode|JoinNode|JunctionNode)[], WhereNode,
	// OrderNode[])
	// JoinNode (Alias, SelectNode)

	// parseJoin(alias, gqlType, aggqlyType, gqlSelectionSet, JoinExpression)

	// parseSelect(gqlType, aggqlyType, gqlSelectionSet, WhereExpression,
	// OrderExpression)
}
