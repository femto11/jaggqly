package org.femto.jaggqlyapp.aggqly.execution;

import java.util.List;

import org.jetbrains.annotations.NotNull;

public record InterfaceNode(@NotNull String alias, @NotNull List<SelectNode> selectNodes)
        implements AstNode {
    public Generated accept(SqlGenerator visitor) {
        return visitor.generate(this);
    }

}