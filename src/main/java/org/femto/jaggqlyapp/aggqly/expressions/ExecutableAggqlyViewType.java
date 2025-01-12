package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.Optional;

public record ExecutableAggqlyViewType(WhereFunction expression, String alias, Optional<ExecutableAggqlyType> ancestor)
        implements ExecutableAggqlyType {
}