package org.femto.jaggqlyapp.aggqly;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.event.KeyValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.DataFetchingFieldSelectionSetImpl;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import graphql.schema.SelectedField;

@Component
public class Aggqly {

    private final AggqlyDataLoaders loaders;

    @Autowired
    public Aggqly(AggqlyDataLoaders loaders) {
        super();
        this.loaders = loaders;
    }

    public String execute(DataFetchingEnvironment dfe) {
        var ast = new Parser(dfe).run();

        return this.generate(ast).statement;
    }

    private Generated generate(Parser.AstNode ast) {
        return switch (ast) {
            case Parser.SelectNode selectNode -> generateSelect(selectNode);
            case Parser.ColumnNode columnNode -> generateColumn(columnNode);
            case Parser.JoinNode joinNode -> generateJoin(joinNode);
            default -> new Generated(null, null, null);
        };
    }

    private Generated generateSelect(Parser.SelectNode node) {
        final var nodes = Generated.normalize(node.selections.stream().map(n -> this.generate(n)).toList());

        var selectPart = "SELECT " + nodes.column;
        var fromPart = "FROM " + "table";
        var joinPart = nodes.join;
        var wherePart = "where";

        return new Generated(selectPart + fromPart + joinPart + wherePart, null, null);
    }

    private Generated generateColumn(Parser.ColumnNode node) {
        return new Generated(null, node.expression + " " + node.alias, null);
    }

    private Generated generateJoin(Parser.JoinNode join) {

        // final var nodes = Generated.normalize(join.selections.stream().map(node ->
        // this.generate(node)).toList());

        return new Generated(
                null,
                join.alias,
                """
                            OUTER APPLY (
                                SELECT ${columns.join(', ')}
                                FROM ${rightTable} AS ${rightTableAlias}
                                ${joins.filter(exists).join()}
                                WHERE ${join.expression}
                                ${where ? `AND ${where}` : ''}
                            ) FOR JSON PATH ${alias}
                        """);
    }

    private record Generated(String statement, String column, String join) {
        public static Generated normalize(List<Generated> list) {
            final var columns = list.stream().map(node -> node.column).collect(Collectors.joining(", "));
            final var joins = list.stream().map(node -> node.join).filter(x -> x != null)
                    .collect(Collectors.joining(" "));

            return new Generated(null, columns, joins);
        }
    };

    private class Parser {

        private DataFetchingEnvironment dfe;

        public Parser(DataFetchingEnvironment dfe) {
            this.dfe = dfe;
        }

        public SelectNode run() {
            var returnType = Parser.<GraphQLObjectType>MaybeUnwrapGraphQLListType(this.dfe.getFieldType());
            if (!(returnType instanceof GraphQLObjectType)) {
                return null;
            }

            final var aggqlyParentType = loaders.getType(((GraphQLObjectType) dfe.getParentType()).getName());

            final var aggqlyType = loaders.getType(((GraphQLObjectType) dfe.getFieldType()).getName());
            if (aggqlyType == null) {
                return null;
            }

            final var gqlField = this.dfe.getField();

            final var aggqlyField = aggqlyParentType.getRoot(gqlField.getName());
            if (aggqlyField == null) {
                return null;
            }

            var selectionSet = this.dfe.getSelectionSet();

            final var selectNode = parseSelect(0, returnType, Map.of(), selectionSet, aggqlyField.getWhere());

            return selectNode;
        }

        private SelectNode parseSelect(Integer level, GraphQLObjectType gqlType, Map<String, Object> args,
                DataFetchingFieldSelectionSet selectionSet, WhereExpression whereExpression) {

            final var aggqlyType = loaders.getType(gqlType.getName());

            final var tableName = aggqlyType.getTable();
            final var tableAlias = Parser.levelledAlias(tableName, level);

            final var selectionNodes = selectionSet.getImmediateFields()
                    .stream()
                    .map(gqlField -> {
                        final var aggqlySelectedField = aggqlyType.getField(gqlField.getName());

                        switch (aggqlySelectedField) {
                            case JoinField joinField -> {
                                return this.parseJoin(level, tableAlias, joinField,
                                        gqlField.getArguments(),
                                        gqlField.getType(),
                                        gqlField.getSelectionSet());
                            }
                            case ColumnField columnField -> {
                                return parseColumn(level, tableAlias, columnField);
                            }
                            default -> {
                                return new DeathNode("");
                            }
                        }
                    })
                    .toList();

            final var whereStatement = whereExpression != null
                    ? whereExpression.method(tableAlias, Map.of(), Map.of())
                    : null;

            return new SelectNode(selectionNodes, whereStatement);
        }

        private AstNode parseJoin(Integer level, String leftTableAlias, JoinField aggqlyField,
                Map<String, Object> args,
                GraphQLOutputType rightRawGqlType, DataFetchingFieldSelectionSet selectionSet) {

            final var rightGqlType = Parser.<GraphQLObjectType>MaybeUnwrapGraphQLListType(rightRawGqlType);

            final var selectNode = this.parseSelect(level + 1, rightGqlType, args, selectionSet,
                    (a, b, c) -> aggqlyField.getExpression().method(leftTableAlias, a, b, c));

            return new JoinNode(aggqlyField.getName(), selectNode);
        }

        private AstNode parseColumn(Integer level, String tableAlias, ColumnField aggqlyField) {
            return new ColumnNode(aggqlyField.getName(), tableAlias + '_' + aggqlyField.getColumn());
        }

        @SuppressWarnings({ "unchecked" })
        private static <T extends GraphQLType> T MaybeUnwrapGraphQLListType(GraphQLType type) {
            return type instanceof GraphQLList
                    ? (T) (((GraphQLList) type).getOriginalWrappedType())
                    : (T) (type);
        }

        private static String levelledAlias(String name, Integer level) {
            return new StringBuilder(name).append('_').append(level).toString();
        }

        public interface AstNode {
        }

        public record DeathNode(@NotNull String alias) implements AstNode {
        };

        public record SelectNode(List<AstNode> selections, String where)
                implements AstNode {
        };

        public record JoinNode(@NotNull String alias, SelectNode selectNode)
                implements AstNode {
        };

        public record ColumnNode(@NotNull String alias, @NotNull String expression) implements AstNode {
        };
    }
}
