package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.Optional;

public interface ExecutableAggqlyType extends SomethingWithAncestor<ExecutableAggqlyType> {
    Optional<ExecutableAggqlyType> ancestor();

    String alias();
}