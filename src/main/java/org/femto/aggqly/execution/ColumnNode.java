package org.femto.aggqly.execution;

import org.jetbrains.annotations.NotNull;

public record ColumnNode(String name, @NotNull String expression)
        implements AstNode {

    public Generated accept(SqlGenerator visitor) {
        return visitor.generate(this);
    }
}