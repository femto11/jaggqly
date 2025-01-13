package org.femto.aggqly.execution;

import org.jetbrains.annotations.NotNull;

public record NullNode(@NotNull String alias) implements AstNode {
    public Generated accept(SqlGenerator visitor) {
        return visitor.generate(this);
    }
}