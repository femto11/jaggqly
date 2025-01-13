package org.femto.aggqly;

import java.util.Map;

import org.springframework.stereotype.Service;

import graphql.schema.DataFetchingEnvironment;
import graphql.util.Pair;

public interface Aggqly {
    public Pair<String, Map<String, Object>> execute(DataFetchingEnvironment dfe);
}
