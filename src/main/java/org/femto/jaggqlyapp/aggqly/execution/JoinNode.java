package org.femto.jaggqlyapp.aggqly.execution;

import java.util.List;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;

public record JoinNode(@NotNull String alias, @NotNull AstNode selectNode, List<Entry<String, String>> orderBy)
        implements AstNode {

    public Generated accept(SqlGenerator visitor) {
        return visitor.generate(this);
    }
}