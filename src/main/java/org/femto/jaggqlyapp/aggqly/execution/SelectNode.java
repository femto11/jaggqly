package org.femto.jaggqlyapp.aggqly.execution;

import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;

import org.femto.jaggqlyapp.aggqly.expressions.ExecutableAggqlyType;
import org.jetbrains.annotations.NotNull;

public record SelectNode(@NotNull ExecutableAggqlyType executableAggqlyType,
                @NotNull List<AstNode> selections,
                Optional<String> where,
                List<Entry<String, String>> orderBy)
                implements AstNode {
        public Generated accept(SqlGenerator visitor) {
                return visitor.generate(this);
        }

}