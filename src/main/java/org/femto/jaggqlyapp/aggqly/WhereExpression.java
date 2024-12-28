package org.femto.jaggqlyapp.aggqly;

import java.util.Map;

@FunctionalInterface
public interface WhereExpression {
    public String method(String tableName, Map<String, String> args);
}