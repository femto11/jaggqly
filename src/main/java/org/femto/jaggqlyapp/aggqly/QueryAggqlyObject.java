package org.femto.jaggqlyapp.aggqly;

import java.util.List;

@AggqlyType(name = "Review", table = "review")
interface Review {
    String text();
}

@AggqlyType(name = "Show", table = "show")
interface Show {
    String title();

    @AggqlyJoin(expression = "/{l:id} = /{r:show_id}")
    List<Review> reviews();
}

@AggqlyType(name = "Query", table = "")
interface Query {
    @AggqlyRoot()
    public List<Show> shows();

    @AggqlyRoot(where = "/{t:title} = /{arg:title}")
    public Show show();
}