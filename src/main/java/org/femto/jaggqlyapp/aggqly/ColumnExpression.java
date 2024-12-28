package org.femto.jaggqlyapp.aggqly;

import java.util.Map;

@FunctionalInterface
public interface ColumnExpression {
    public String method(String table, Map<String, String> args, Map<String, String> ctx);
}