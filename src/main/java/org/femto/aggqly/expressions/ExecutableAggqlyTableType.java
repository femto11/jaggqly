package org.femto.aggqly.expressions;

import java.util.Optional;

public record ExecutableAggqlyTableType(Optional<String> db, Optional<String> schema, String table,
                String alias, Optional<ExecutableAggqlyType> ancestor) implements ExecutableAggqlyType {
}