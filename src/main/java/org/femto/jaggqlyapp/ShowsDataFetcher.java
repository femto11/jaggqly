package org.femto.jaggqlyapp;

import java.util.List;
import org.femto.jaggqlyapp.aggqly.Aggqly;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import graphql.schema.DataFetchingEnvironment;

import org.femto.jaggqlyapp.codegen.types.Show;

@DgsComponent
public class ShowsDataFetcher {

  record Review(String text) {
  }

  // record Show(String title, int releaseYear, List<Review> reviews) {
  // }

  @Autowired
  Aggqly aggqly;

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

    final var mapper = new ObjectMapper();

    try {
      final var shows = mapper.readValue(json, new TypeReference<List<Show>>() {
      });

      return shows;
    } catch (JsonProcessingException e) {

    }
    return List.of();
  }

  @DgsQuery
  public Show show(DataFetchingEnvironment dfe) {
    System.out.println(this.aggqly.execute(dfe));
    return null;
  }
}