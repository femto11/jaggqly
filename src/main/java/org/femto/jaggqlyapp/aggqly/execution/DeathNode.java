package org.femto.jaggqlyapp.aggqly.execution;

import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

public record DeathNode(@NotNull String alias) implements AstNode {
    public Generated accept(SqlGenerator visitor) {
        throw new NotImplementedException();
    }
}