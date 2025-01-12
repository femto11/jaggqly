package org.femto.jaggqlyapp.aggqly.execution;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.femto.jaggqlyapp.aggqly.expressions.ExecutableAggqlyTableType;
import org.femto.jaggqlyapp.aggqly.expressions.ExecutableAggqlyType;
import org.jetbrains.annotations.NotNull;

public class TSqlGenerator implements SqlGenerator {

    @Override
    public Generated generate(SelectNode node) {
        final var nodes = Generated.normalize(
                node.selections()
                        .stream()
                        .map(n -> n.accept(this))
                        .toList());

        var wherePart = node.where().isPresent() ? "WHERE " + node.where().get() + " " : "";

        final var selectStatment = MessageFormat.format(
                "SELECT {0} FROM {1} {2} {3} {4}",
                nodes.column(),
                generateTable(node.executableAggqlyType()),
                nodes.join(),
                wherePart,
                generateOrderBy(List.of()));

        return new Generated(selectStatment, null, null);
    }

    @Override
    public Generated generate(JoinNode node) {
        final var generated = node.selectNode().accept(this);

        var sql = "OUTER APPLY (" + generated.statement() + " FOR JSON PATH) "
                + node.alias() + "(" + sqlId(node.alias()) + ")";

        return new Generated(null, sqlId(node.alias()), sql);
    }

    @Override
    public Generated generate(InterfaceNode node) {
        final var union = node.selectNodes()
                .stream()
                .map(n -> n.accept(this).statement())
                .collect(Collectors.joining(" UNION "));

        return new Generated(union + " " + node.alias() + " ", "", "");
    }

    @Override
    public Generated generate(ColumnNode node) {
        return new Generated(null, node.expression() + " " + sqlId(node.name()) + " ", null);
    }

    @Override
    public Generated generate(NullNode node) {
        return new Generated(null, "", null);
    }

    private static String generateOrderBy(List<Entry<String, String>> orderByList) {
        return !orderByList.isEmpty()
                ? "ORDER BY " + orderByList
                        .stream()
                        .map(e -> sqlId(e.getKey()) + ' ' + e.getValue())
                        .collect(Collectors.joining(", "))
                : "";
    }

    private static String generateTable(@NotNull ExecutableAggqlyType executableAggqlyType) {
        if (executableAggqlyType instanceof ExecutableAggqlyTableType tableType) {
            return tableType.schema().isPresent()
                    ? tableType.schema().get() + "." + tableType.table() + " " + tableType.alias()
                    : tableType.table() + " " + tableType.alias();
        }

        throw new RuntimeException("generateTable for ExecutableAggqlyTableType is not implemented");
    }

    private static String sqlId(@NotNull String id) {
        return "[" + id + "]";
    }
}
