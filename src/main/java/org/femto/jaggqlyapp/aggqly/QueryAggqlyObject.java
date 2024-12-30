package org.femto.jaggqlyapp.aggqly;

import java.util.List;

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

@AggqlyType(name = "Show", table = "show")
interface Show {
    String title();

    @AggqlyJoin(expression = "/{l:show_id} = /{r:show_id}")
    List<Review> reviews();
}

@AggqlyType(name = "Query", table = "")
interface Query {
    @AggqlyRoot()
    public List<Show> shows();

    @AggqlyRoot(where = "/{t:title} = /{arg:title}")
    public Show show();
}