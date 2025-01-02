package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.Map;

@FunctionalInterface
public interface JoinFunction {
    String get(String l, String r, Map<String, String> args, Map<String, String> ctx);

    default WhereFunction reduce(String l) {
        return (r, args, ctx) -> this.get(l, r, args, ctx);
    }
}