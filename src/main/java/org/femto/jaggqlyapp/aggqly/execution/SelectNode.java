package org.femto.jaggqlyapp.aggqly.execution;

import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;

import org.jetbrains.annotations.NotNull;

public record SelectNode(@NotNull Optional<String> schema, @NotNull String table, @NotNull String alias,
                @NotNull List<AstNode> selections,
                Optional<String> where, List<Entry<String, String>> orderBy)
                implements AstNode {
        public Generated accept(SqlGenerator visitor) {
                return visitor.generate(this);
        }

}