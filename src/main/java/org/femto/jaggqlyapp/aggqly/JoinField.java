package org.femto.jaggqlyapp.aggqly;

import org.femto.jaggqlyapp.aggqly.impl.JoinExpressionImpl;
import org.jetbrains.annotations.NotNull;

public final class JoinField implements AggqlyField {
    private final String name;
    private JoinExpression expression;

    private JoinField(
            @NotNull String name,
            @NotNull JoinExpression expression) {
        this.name = name;
        this.expression = expression;
    }

    public static JoinField fromAnnotation(
            @NotNull String name,
            @NotNull AggqlyJoin annotation) {
        final var expression = new JoinExpressionImpl(annotation.expression());
        return new JoinField(name, expression);
    }

    @Override
    public String getName() {
        return this.name;
    }

    public JoinExpression getExpression() {
        return this.expression;
    }
}
