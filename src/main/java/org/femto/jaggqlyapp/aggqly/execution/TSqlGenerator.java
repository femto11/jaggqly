package org.femto.jaggqlyapp.aggqly.execution;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

public class TSqlGenerator implements SqlGenerator {

    @Override
    public Generated generate(SelectNode node) {
        final var nodes = Generated.normalize(node.selections().stream().map(n -> n.accept(this)).toList());

        var wherePart = node.where().isPresent() ? "WHERE " + node.where().get() + " " : "";

        final var selectStatment = MessageFormat.format(
                "SELECT {0} FROM {1} {2} {3} {4} {5}", nodes.column(),
                generateTable(node.schema(), node.table()), node.alias(),
                nodes.join(), wherePart, generateOrderBy(List.of()));

        return new Generated(selectStatment, null, null);
    }

    @Override
    public Generated generate(JoinNode node) {
        final var generated = node.selectNode().accept(this);

        return new Generated(
                null,
                "(SELECT * FROM (" + generated.statement() + ") " + sqlId(node.alias())
                        + " " + generateOrderBy(node.orderBy()) + " FOR JSON PATH) "
                        + sqlId(node.alias()),
                null);
    }

    @Override
    public Generated generate(InterfaceNode node) {
        final var selectStatements = node.selectNodes()
                .stream()
                .map(n -> n.accept(this).statement())
                .toList();

        return new Generated(String.join(" UNION ", selectStatements) + " " + node.alias() + " ", "", "");
    }

    @Override
    public Generated generate(ColumnNode node) {
        return new Generated(null, node.expression() + " " + sqlId(node.alias()) + " ", null);
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

    private static String generateTable(@NotNull Optional<String> schema, @NotNull String table) {
        return schema.isPresent()
                ? schema.get() + "." + table
                : table;
    }

    private static String sqlId(@NotNull String id) {
        return "\"" + id + "\"";
    }
}
