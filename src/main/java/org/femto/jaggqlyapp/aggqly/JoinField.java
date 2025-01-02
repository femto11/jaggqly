package org.femto.jaggqlyapp.aggqly;

import org.femto.jaggqlyapp.aggqly.expressions.JoinFunction;
import org.jetbrains.annotations.NotNull;

public final class JoinField implements AggqlyField {
    private final String name;
    private JoinFunction expression;

    private JoinField(
            @NotNull String name,
            @NotNull JoinFunction expression) {
        this.name = name;
        this.expression = expression;
    }

    public static JoinField fromAnnotation(
            @NotNull String name,
            @NotNull AggqlyJoin annotation) {
        final var expression = JoinFunction.fromExpression(annotation.expression());
        return new JoinField(name, expression);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public JoinFunction getExpression() {
        return this.expression;
    }
}
