package org.femto.jaggqlyapp;

import java.util.List;
import org.femto.aggqly.Aggqly;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsQuery;
import graphql.schema.DataFetchingEnvironment;

import org.femto.jaggqlyapp.codegen.types.Show;

@DgsComponent
public class ShowsDataFetcher {

  final private Aggqly aggqly;
  final private DbFetcher db;

  record Review(String text) {
  }

  // record Show(String title, int releaseYear, List<Review> reviews) {
  // }

  @Autowired
  ShowsDataFetcher(Aggqly aggqly, DbFetcher fetcher) {
    this.aggqly = aggqly;
    this.db = fetcher;
  }

  @DgsQuery
  public List<Show> categories(DataFetchingEnvironment dfe) {
    System.out.println(this.aggqly.execute(dfe));

    return List.of();
  }

  @DgsQuery
  public List<Show> shows(DataFetchingEnvironment dfe) {
    final var a = this.aggqly.execute(dfe);

    System.out.println(a);

    var json = "[{\"title\":\"Stranger Things\",\"releaseYear\":2016,\"actors\":[{\"name\":\"Joyce Beyers\"},{\"name\":\"Holly Wheeler\"}]},{\"title\":\"Ozark\",\"releaseYear\":2017,\"actors\":[{\"name\":\"Jason Bateman\"}]}]";

    db.fetch(a.first);

    final var mapper = new ObjectMapper();

    try {
      final var shows = mapper.readValue(json, new TypeReference<List<Show>>() {
      });

      return shows;
    } catch (JsonProcessingException e) {

    }
    return List.of();
  }

  @DgsData(parentType = "Show", field = "actors")
  public List<org.femto.jaggqlyapp.codegen.types.Actor> actors(DataFetchingEnvironment dfe) {
    var s = (Show) dfe.getSource();

    return s.getActors();
  }

  @DgsQuery
  public Show show(DataFetchingEnvironment dfe) {
    System.out.println(this.aggqly.execute(dfe));
    return null;
  }
}