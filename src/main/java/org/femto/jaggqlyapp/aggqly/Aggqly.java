package org.femto.jaggqlyapp.aggqly;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
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

        return new Generated(
                """
                                SELECT columns
                                FROM table
                                ...joins
                                WHERE where
                        """, null, null);
    }

    private Generated generateColumn(Parser.ColumnNode node) {
        return new Generated(null, node.expression + " " + node.alias, null);
    }

    private Generated generateJoin(Parser.JoinNode join) {
        final var nodes = Generated.normalize(join.selections.stream().map(node -> this.generate(node)).toList());

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
            var args = this.dfe.getArguments();
            var selectionSet = this.dfe.getSelectionSet();
            var returnType = Parser.<GraphQLObjectType>MaybeUnwrapGraphQLListType(this.dfe.getFieldType());

            if (returnType instanceof GraphQLObjectType) {
                final var selections = iterate(1, (GraphQLObjectType) returnType, args, selectionSet);
                return new SelectNode(selections);
            }

            return null;
        }

        private List<AstNode> iterate(Integer level, GraphQLObjectType returnType, Map<String, Object> args,
                DataFetchingFieldSelectionSet selectionSet) {
            final var aggqlyType = loaders.getType(returnType.getName());

            return selectionSet.getImmediateFields()
                    .stream()
                    .map(field -> {
                        var aggqlyField = aggqlyType.getField(field.getName());

                        return switch (aggqlyField) {
                            case JoinField joinField -> parseJoin(level, aggqlyType, field, joinField);
                            case ColumnField columnField -> parseColumn(level, aggqlyType, field, columnField);
                            default -> (AstNode) new Parser.DeathNode("Foo");
                        };
                    }).toList();
        }

        private AstNode parseColumn(Integer level, AggqlyObject aggqlyType, SelectedField gqlField,
                ColumnField aggqlyField) {
            var tableAlias = Parser.levelledAlias(aggqlyType.getTable(), level);

            // TODO: create the expression in the generator or provide an expression lambda
            // in ColumnField
            return new Parser.ColumnNode(tableAlias + '.' + ((ColumnField) aggqlyField).column,
                    gqlField.getName());

        }

        private AstNode parseJoin(Integer level, AggqlyObject aggqlyType, SelectedField gqlField,
                JoinField aggqlyField) {
            var gqlChildType = Parser.<GraphQLObjectType>MaybeUnwrapGraphQLListType(gqlField.getType());

            var aggqlyJoinField = (JoinField) aggqlyField;
            var aggqlyJoinedType = loaders.getType(gqlChildType.getName());

            final var leftTableAlias = Parser.levelledAlias(aggqlyType.getTable(), level);
            final var rightTableAlias = Parser.levelledAlias(aggqlyJoinedType.getTable(), level + 1);

            // TODO: arguments
            // 'arguments' in selection && selection.arguments?.forEach(arg => {
            // paramValues[arg.name.value] = arg.value;
            // paramRegistry[arg.name.value] = `:${levelledAlias(arg.name.value, level)}_${i
            // + 1}`
            // })

            final var expression = aggqlyJoinField.getExpression().method(leftTableAlias,
                    rightTableAlias, Map.of(), Map.of());

            final var ast = iterate(level + 1, gqlChildType, Map.of(), gqlField.getSelectionSet());

            return (AstNode) new Parser.JoinNode(gqlField.getName(), expression, ast);
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

        public record SelectNode(List<AstNode> selections)
                implements AstNode {
        };

        public record JoinNode(@NotNull String alias, @NotNull String expression, List<AstNode> selections)
                implements AstNode {
        };

        public record ColumnNode(@NotNull String alias, @NotNull String expression) implements AstNode {
        };
    }
}
