package org.femto.aggqly.execution;

import org.jetbrains.annotations.NotNull;

public record DeathNode(@NotNull String alias) implements AstNode {
    public Generated accept(SqlGenerator visitor) {
        throw new RuntimeException();
    }
}