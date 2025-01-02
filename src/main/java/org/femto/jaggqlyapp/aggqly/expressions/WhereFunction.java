package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.Map;

@FunctionalInterface
public interface WhereFunction {
    String get(String t, Map<String, String> args, Map<String, String> ctx);
}