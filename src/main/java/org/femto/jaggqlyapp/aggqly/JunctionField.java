package org.femto.jaggqlyapp.aggqly;

import org.femto.jaggqlyapp.aggqly.expressions.JoinFunction;
import org.jetbrains.annotations.NotNull;

public final class JunctionField implements AggqlyField {
    private final String name;
    private final JoinFunction expression;

    private JunctionField(
            @NotNull String name,
            @NotNull JoinFunction expression) {
        this.name = name;
        this.expression = expression;
    }

    public static JunctionField fromAnnotation(
            @NotNull String name,
            @NotNull AggqlyJunction annotation) {
        final var expression = JoinFunction.fromExpression(annotation.expression());
        return new JunctionField(name, expression);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public JoinFunction getExpression() {
        return this.expression;
    }
}
