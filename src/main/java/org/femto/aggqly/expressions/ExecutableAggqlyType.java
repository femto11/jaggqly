package org.femto.aggqly.expressions;

import java.util.Optional;

public interface ExecutableAggqlyType extends SomethingWithAncestor<ExecutableAggqlyType> {
    Optional<ExecutableAggqlyType> ancestor();

    String alias();
}