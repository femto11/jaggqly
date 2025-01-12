package org.femto.jaggqlyapp;

import java.util.List;

import org.femto.jaggqlyapp.aggqly.schema.AggqlyColumn;
import org.femto.jaggqlyapp.aggqly.schema.AggqlyJoin;
import org.femto.jaggqlyapp.aggqly.schema.AggqlyJunction;
import org.femto.jaggqlyapp.aggqly.schema.AggqlyRoot;
import org.femto.jaggqlyapp.aggqly.schema.AggqlyType;

@AggqlyType(name = "Review", table = "")
interface Review {
    Integer rating();
}

@AggqlyType(name = "FullReview", table = "full_review")
interface FullReview extends Review {
    Integer rating();

    String comment();
}

@AggqlyType(name = "ShortReview", table = "simple_review")
interface ShortReview extends Review {
    Integer rating();
}

@AggqlyType(name = "Show", table = "show", schema = "dbo")
interface Show {
    String title();

    @AggqlyJoin(expression = "{!l(show_id)} = {!r(show_id)}")
    List<Review> reviews();

    @AggqlyJunction(expression = """
            EXISTS (
                SELECT null
                FROM show_actor sa
                WHERE {!l(show_id)} = sa.show_id
                AND {!r(actor_id)} = sa.actor_id
            )""")
    List<Actor> actors();
}

@AggqlyType(name = "Actor", table = "actor")
interface Actor {
    String name();
}

@AggqlyType(name = "Category", table = "category", expression = "SELECT DISTINCT category FROM show")
interface Category {
    @AggqlyColumn(column = "category")
    String name();

    @AggqlyJoin(expression = "{!l(category)} = {!r(category)}")
    List<Show> shows();
}

@AggqlyType(name = "Query", table = "")
interface Query {
    @AggqlyRoot()
    public List<Object> categories();

    @AggqlyRoot()
    public List<Show> shows();

    @AggqlyRoot(where = "{!t(title)} = {!arg(title)}")
    public Show show();
}