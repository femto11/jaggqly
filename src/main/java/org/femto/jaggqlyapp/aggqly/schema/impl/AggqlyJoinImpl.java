package org.femto.jaggqlyapp.aggqly.schema.impl;

import org.femto.jaggqlyapp.aggqly.expressions.JoinFunction;
import org.femto.jaggqlyapp.aggqly.schema.AggqlyJoin;
import org.jetbrains.annotations.NotNull;

final class AggqlyJoinImpl implements AggqlyField {
    private final String name;
    private JoinFunction expression;

    private AggqlyJoinImpl(
            @NotNull String name,
            @NotNull JoinFunction expression) {
        this.name = name;
        this.expression = expression;
    }

    public static AggqlyJoinImpl fromAnnotation(
            @NotNull String name,
            @NotNull AggqlyJoin annotation) {
        final var expression = JoinFunction.fromExpression(annotation.expression());
        return new AggqlyJoinImpl(name, expression);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public JoinFunction getExpression() {
        return this.expression;
    }
}
