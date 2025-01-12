package org.femto.jaggqlyapp.aggqly;

import java.util.Map;

import graphql.schema.DataFetchingEnvironment;
import graphql.util.Pair;

public interface Aggqly {
    public Pair<String, Map<String, Object>> execute(DataFetchingEnvironment dfe);
}
