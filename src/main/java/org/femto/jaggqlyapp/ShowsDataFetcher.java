package org.femto.jaggqlyapp;

import java.util.List;
import org.femto.jaggqlyapp.aggqly.Aggqly;
import org.springframework.beans.factory.annotation.Autowired;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import graphql.schema.DataFetchingEnvironment;

@DgsComponent
public class ShowsDataFetcher {

  @Autowired
  Aggqly aggqly;

  private final List<Show> shows = List.of(
      new Show("Stranger Things", 2016, List.of()),
      new Show("Ozark", 2017, List.of()),
      new Show("The Crown", 2016, List.of()),
      new Show("Dead to Me", 2019, List.of()),
      new Show("Orange is the New Black", 2013, List.of()));

  @DgsQuery
  public List<Show> shows(DataFetchingEnvironment dfe) {
    this.aggqly.execute(dfe);
    return shows;
  }

  @DgsQuery
  public Show show(DataFetchingEnvironment dfe) {
    System.out.println(this.aggqly.execute(dfe));
    return shows.get(0);
  }
}

record Review(String text) {
}

record Show(String title, int releaseYear, List<Review> reviews) {
}