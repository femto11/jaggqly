package org.femto.jaggqlyapp.aggqly.schema.impl;

import org.femto.jaggqlyapp.aggqly.expressions.JoinFunction;
import org.femto.jaggqlyapp.aggqly.expressions.ParserException;
import org.femto.jaggqlyapp.aggqly.schema.AggqlyJunction;
import org.jetbrains.annotations.NotNull;

final class AggqlyJunctionImpl implements AggqlyField {
    private final String name;
    private final JoinFunction expression;

    private AggqlyJunctionImpl(
            @NotNull String name,
            @NotNull JoinFunction expression) {
        this.name = name;
        this.expression = expression;
    }

    public static AggqlyJunctionImpl fromAnnotation(
            @NotNull String name,
            @NotNull AggqlyJunction annotation) throws ParserException {
        final var expression = JoinFunction.fromExpression(annotation.expression());
        return new AggqlyJunctionImpl(name, expression);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public JoinFunction getExpression() {
        return this.expression;
    }
}
