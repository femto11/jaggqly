package org.femto.jaggqlyapp.aggqly.execution;

import org.jetbrains.annotations.NotNull;

public record ColumnNode(@NotNull String alias, @NotNull String expression) implements AstNode {
    public Generated accept(SqlGenerator visitor) {
        return visitor.generate(this);
    }
}