package org.femto.jaggqlyapp.aggqly;

import java.util.Map;

@FunctionalInterface
public interface JoinExpression {
    public String method(String ltable, String rtable, Map<String, String> args, Map<String, String> ctx);
}
