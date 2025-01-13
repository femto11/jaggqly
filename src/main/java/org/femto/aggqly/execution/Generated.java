package org.femto.aggqly.execution;

import java.util.List;
import java.util.stream.Collectors;

public record Generated(String statement, String column, String join) {
    public static Generated normalize(List<Generated> list) {
        final var columns = list.stream().map(node -> node.column).collect(Collectors.joining(", "));
        final var joins = list.stream().map(node -> node.join).filter(x -> x != null)
                .collect(Collectors.joining(" "));

        return new Generated(null, columns, joins);
    }
}