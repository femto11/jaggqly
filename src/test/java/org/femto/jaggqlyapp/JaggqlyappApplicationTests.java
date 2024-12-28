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

		var e = new JoinExpressionImpl("""
				/{l:right_id} = /{r:id}
				AND /{l:age} < /{arg:$.$.age}
				""");

		var sql = e.method("ltab", "rtab", Map.of("age", ":age_1"), Map.of());

		System.out.println(sql);
	}

	@Test
	void gqlQueryTest() {
		var r = dgsQueryExecutor.execute("""
					query {
						shows {
							...showFields
						}
					}
					fragment showFields on Show {
						title, reviews { text }
					}
				""");

		System.out.println(r);
	}

}
