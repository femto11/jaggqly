package org.femto.jaggqlyapp.aggqly;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.femto.jaggqlyapp.aggqly.execution.AstNode;
import org.femto.jaggqlyapp.aggqly.execution.ColumnNode;
import org.femto.jaggqlyapp.aggqly.execution.DeathNode;
import org.femto.jaggqlyapp.aggqly.execution.InterfaceNode;
import org.femto.jaggqlyapp.aggqly.execution.JoinNode;
import org.femto.jaggqlyapp.aggqly.execution.NullNode;
import org.femto.jaggqlyapp.aggqly.execution.SelectNode;
import org.femto.jaggqlyapp.aggqly.execution.SqlGenerator;
import org.femto.jaggqlyapp.aggqly.execution.TSqlGenerator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import graphql.com.google.common.collect.ImmutableList;
import graphql.language.Argument;
import graphql.language.EnumValue;
import graphql.language.ObjectValue;
import graphql.normalized.ExecutableNormalizedField;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
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

    public Pair<String, Map<String, Object>> execute(DataFetchingEnvironment dfe) {
        var parseResult = new Parser(dfe).run();

        final var generator = new TSqlGenerator();

        final var sql = parseResult.getLeft().accept(generator).statement();

        return Pair.of(sql, parseResult.getRight());
    }

    private class Parser {
        private final DataFetchingEnvironment dfe;

        private final Map<String, Object> registeredParameters;

        public Parser(DataFetchingEnvironment dfe) {
            this.dfe = dfe;

            this.registeredParameters = new HashMap<String, Object>();
        }

        public Pair<SelectNode, Map<String, Object>> run() {
            var returnType = Parser.<GraphQLObjectType>MaybeUnwrapGraphQLListType(this.dfe.getFieldType());
            if (!(returnType instanceof GraphQLObjectType)) {
                return null;
            }

            final var aggqlyType = loaders.getType(returnType.getName());
            if (aggqlyType == null) {
                return null;
            }

            final var gqlField = this.dfe.getField();
            final var gqlFieldDef = this.dfe.getFieldDefinition();

            final var aggqlyParentType = loaders.getType(((GraphQLObjectType) dfe.getParentType()).getName());
            final var aggqlyField = aggqlyParentType.getRoot(gqlField.getName());
            if (aggqlyField == null) {
                return null;
            }

            var orderBy = parseOrderByArgument(ImmutableList.copyOf(gqlField.getArguments()),
                    gqlFieldDef.getArguments());

            var selectNode = parseSelect(0, returnType, this.dfe.getArguments(), this.dfe.getSelectionSet(),
                    aggqlyField.getWhere(), orderBy);

            return Pair.of(selectNode, Collections.unmodifiableMap(this.registeredParameters));
        }

        private static ExecutableNormalizedField doHack(SelectedField sf) {
            try {
                var hacked = sf.getClass().getDeclaredField("executableNormalizedField");
                hacked.setAccessible(true);
                return (ExecutableNormalizedField) hacked.get(sf);
            } catch (NoSuchFieldException e) {
            } catch (IllegalAccessException e) {
            }

            return null;
        }

        private SelectNode parseSelect(Integer level, GraphQLObjectType gqlType, Map<String, Object> args,
                DataFetchingFieldSelectionSet selectionSet, WhereExpression whereExpression,
                List<Entry<String, String>> orderBy) {

            final var aggqlyType = loaders.getType(gqlType.getName());

            final var schemaName = aggqlyType.getSchema();
            final var tableName = aggqlyType.getTable();
            final var tableAlias = Parser.levelledAlias(tableName, level);
            final var tableExpression = aggqlyType.getExpression().isPresent()
                    ? aggqlyType.getExpression().get().get(null, null, Map.of())
                    : tableName;

            final var selectionNodes = selectionSet.getImmediateFields()
                    .stream()
                    .map(gqlField -> {
                        final var aggqlySelectedField = aggqlyType.getField(gqlField.getName());

                        if (aggqlySelectedField == null) {
                            return new NullNode(gqlField.getName());
                        }

                        switch (aggqlySelectedField) {
                            case JoinField joinField -> {
                                return this.parseJoin(level, tableAlias, joinField,
                                        gqlField.getArguments(),
                                        gqlField.getType(),
                                        gqlField.getSelectionSet(),
                                        parseOrderByArgument(Parser.doHack(gqlField).getAstArguments(),
                                                gqlField.getFieldDefinitions().getFirst().getArguments()));
                            }
                            case JunctionField junctionField -> {
                                return this.parseJunction(level, tableAlias, junctionField,
                                        gqlField.getArguments(),
                                        gqlField.getType(),
                                        gqlField.getSelectionSet(),
                                        parseOrderByArgument(Parser.doHack(gqlField).getAstArguments(),
                                                gqlField.getFieldDefinitions().getFirst().getArguments()));
                            }
                            case ComputedField computedField -> {
                                return parseComputed(level, tableAlias, computedField);
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

            final var argAliases = args.entrySet()
                    .stream()
                    .map(arg -> {
                        final var alias = this.registerParameter(level, arg.getKey(), arg.getValue());
                        return Map.entry(arg.getKey(), alias);
                    })
                    .collect(Collectors.toUnmodifiableMap(x -> x.getKey(), x -> x.getValue()));

            final var whereStatement = whereExpression != null
                    ? Optional.of(whereExpression.method(tableAlias, argAliases, Map.of()))
                    : Optional.<String>empty();

            return new SelectNode(schemaName, tableExpression, tableAlias, selectionNodes, whereStatement, orderBy);
        }

        private AstNode parseJoin(Integer level, String leftTableAlias, JoinField aggqlyField,
                Map<String, Object> args,
                GraphQLOutputType rightRawGqlType,
                DataFetchingFieldSelectionSet selectionSet,
                List<Entry<String, String>> orderBy) {

            final var unwrappedGqlType = Parser.MaybeUnwrapGraphQLListType(rightRawGqlType);

            final WhereExpression preparedJoinExpression = (a, b, c) -> aggqlyField.getExpression()
                    .get(leftTableAlias, a, b, c);

            final AstNode selectNode = switch (unwrappedGqlType) {
                case GraphQLInterfaceType interfaceType ->
                    this.parseInterface(level, interfaceType, args, selectionSet, preparedJoinExpression);
                case GraphQLObjectType objectType ->
                    this.parseSelect(level + 1, objectType, args, selectionSet, preparedJoinExpression,
                            List.of());
                default -> new DeathNode("");
            };

            return new JoinNode(aggqlyField.getName(), selectNode, orderBy);
        }

        private AstNode parseJunction(Integer level, String leftTableAlias, JunctionField aggqlyField,
                Map<String, Object> args,
                GraphQLOutputType rightRawGqlType,
                DataFetchingFieldSelectionSet selectionSet,
                List<Entry<String, String>> orderBy) {
            final var unwrappedGqlType = Parser.MaybeUnwrapGraphQLListType(rightRawGqlType);

            final WhereExpression preparedJoinExpression = (a, b, c) -> aggqlyField.getExpression()
                    .get(leftTableAlias, a, b, c);

            final AstNode selectNode = switch (unwrappedGqlType) {
                case GraphQLInterfaceType interfaceType ->
                    this.parseInterface(level, interfaceType, args, selectionSet, preparedJoinExpression);
                case GraphQLObjectType objectType ->
                    this.parseSelect(level + 1, objectType, args, selectionSet, preparedJoinExpression,
                            List.of());
                default -> new DeathNode("");
            };

            return new JoinNode(aggqlyField.getName(), selectNode, orderBy);
        };

        private AstNode parseInterface(Integer level, GraphQLInterfaceType gqlIfType,
                Map<String, Object> args, DataFetchingFieldSelectionSet selectionSet, WhereExpression whereExpression) {

            final var selectedGqlTypes = selectionSet.getFields()
                    .stream()
                    .map(f -> f.getObjectTypes())
                    .flatMap(List::stream)
                    .collect(Collectors.toUnmodifiableSet());

            if (selectedGqlTypes.size() == 1) {
                return parseSelect(level, selectedGqlTypes.stream().findFirst().get(), args,
                        selectionSet, whereExpression, List.of());
            }

            final var selectNodes = selectedGqlTypes
                    .stream()
                    .map(gqlType -> {
                        return parseSelect(level, gqlType, args, selectionSet, whereExpression, List.of());
                    })
                    .toList();

            return new InterfaceNode("", selectNodes);
        }

        private AstNode parseComputed(Integer level, String tableAlias, ComputedField aggqlyField) {
            return new ColumnNode(aggqlyField.getName(), aggqlyField.getFunction().get(tableAlias, Map.of(), Map.of()));
        }

        private AstNode parseColumn(Integer level, String tableAlias, ColumnField aggqlyField) {
            return new ColumnNode(aggqlyField.getName(), tableAlias + '.' + aggqlyField.getColumn());
        }

        private String registerParameter(Integer level, String name, Object value) {
            for (int i = 0; true; i++) {
                final var alias = name + "_" + Integer.toString(i);
                if (!this.registeredParameters.containsKey(alias)) {
                    this.registeredParameters.put(alias, value);
                    return alias;
                }
            }
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

        List<Entry<String, String>> parseOrderByArgument(ImmutableList<Argument> args,
                List<GraphQLArgument> argDefinitions) {
            var orderByArgsDefinition = argDefinitions
                    .stream()
                    .filter(ad -> {
                        return ad.getType() instanceof GraphQLInputObjectType iot
                                ? iot.getDirective("orderByInput") != null
                                : false;
                    }).findFirst();

            var orderBy = orderByArgsDefinition.isPresent()
                    ? args
                            .stream()
                            .filter(e -> e.getName().equals(orderByArgsDefinition.get().getName()))
                            .flatMap(e -> ((ObjectValue) e.getValue()).getObjectFields().stream())
                            .map(e -> Map.entry(e.getName(), ((EnumValue) e.getValue()).getName()))
                            .toList()
                    : List.<Entry<String, String>>of();

            return orderBy;
        }
    }
}
