package org.femto.aggqly.schema.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.femto.aggqly.Aggqly;
import org.femto.aggqly.execution.AstNode;
import org.femto.aggqly.execution.ColumnNode;
import org.femto.aggqly.execution.DeathNode;
import org.femto.aggqly.execution.InterfaceNode;
import org.femto.aggqly.execution.JoinNode;
import org.femto.aggqly.execution.NullNode;
import org.femto.aggqly.execution.SelectNode;
import org.femto.aggqly.execution.TSqlGenerator;
import org.femto.aggqly.expressions.ExecutableAggqlyTableType;
import org.femto.aggqly.expressions.ExecutableAggqlyType;
import org.femto.aggqly.expressions.ExecutableAggqlyViewType;
import org.femto.aggqly.expressions.MapWithAncestor;
import org.femto.aggqly.expressions.WhereFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

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
import graphql.util.Pair;

interface ReflectionUtils {
    static ExecutableNormalizedField getField(SelectedField sf) {
        try {
            var hacked = sf.getClass().getDeclaredField("executableNormalizedField");
            hacked.setAccessible(true);
            return (ExecutableNormalizedField) hacked.get(sf);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }

        return null;
    }
}

@Service
final class AggqlyImpl implements Aggqly {

    private final AggqlyDataLoaders loaders;

    @Autowired
    public AggqlyImpl(AggqlyDataLoaders loaders) {
        super();
        this.loaders = loaders;
    }

    public Pair<String, Map<String, Object>> execute(DataFetchingEnvironment dfe) {
        var parseResult = new Parser(dfe).run();

        final var generator = new TSqlGenerator();

        final var sql = parseResult.first.accept(generator).statement();

        return Pair.pair(sql, parseResult.second);
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

            var selectNode = parseSelect(0, Optional.empty(), Optional.empty(), returnType, this.dfe.getArguments(),
                    this.dfe.getSelectionSet(), aggqlyField.getWhere(), orderBy);

            return Pair.pair(selectNode, Collections.unmodifiableMap(this.registeredParameters));
        }

        private SelectNode parseSelect(Integer level,
                Optional<ExecutableAggqlyType> previousExecutableAggqlyType,
                Optional<MapWithAncestor<String, String>> previousArgAliases,
                GraphQLObjectType gqlType,
                Map<String, Object> args,
                DataFetchingFieldSelectionSet selectionSet, WhereFunction whereExpression,
                List<Entry<String, String>> orderBy) {

            final var aggqlyType = loaders.getType(gqlType.getName());

            final var executableAggqlyType = aggqlyType.getExpression().isPresent()
                    ? new ExecutableAggqlyViewType(null, Parser.levelledAlias(aggqlyType.getTable(), level),
                            previousExecutableAggqlyType)
                    : new ExecutableAggqlyTableType(Optional.empty(), aggqlyType.getSchema(), aggqlyType.getTable(),
                            Parser.levelledAlias(aggqlyType.getTable(), level), previousExecutableAggqlyType);

            final var selectionNodes = selectionSet.getImmediateFields()
                    .stream()
                    .map(gqlField -> {
                        final var aggqlySelectedField = aggqlyType.getField(gqlField.getName());

                        if (aggqlySelectedField == null) {
                            return new NullNode(gqlField.getName());
                        }

                        switch (aggqlySelectedField) {
                            case AggqlyJoinImpl joinField -> {
                                return this.parseJoin(level, executableAggqlyType,
                                        previousArgAliases,
                                        joinField,
                                        gqlField.getArguments(),
                                        gqlField.getType(),
                                        gqlField.getSelectionSet(),
                                        parseOrderByArgument(ReflectionUtils.getField(gqlField).getAstArguments(),
                                                gqlField.getFieldDefinitions().getFirst().getArguments()));
                            }
                            case AggqlyJunctionImpl junctionField -> {
                                return this.parseJunction(level, executableAggqlyType,
                                        previousArgAliases,
                                        junctionField,
                                        gqlField.getArguments(),
                                        gqlField.getType(),
                                        gqlField.getSelectionSet(),
                                        parseOrderByArgument(ReflectionUtils.getField(gqlField).getAstArguments(),
                                                gqlField.getFieldDefinitions().getFirst().getArguments()));
                            }
                            case AggqlyComputedImpl computedField -> {
                                return parseComputed(level, computedField);
                            }
                            case AggqlyColumnImpl columnField -> {
                                return parseColumn(level, columnField);
                            }
                            default -> {
                                return new DeathNode("");
                            }
                        }
                    })
                    .toList();

            final var argAliases = args.entrySet()
                    .stream()
                    .collect(Collectors.toUnmodifiableMap(x -> x.getKey(),
                            x -> this.registerParameter(level, x.getKey(), x.getValue())));
            final var argAliasesWithAncestor = new MapWithAncestor<String, String>(previousArgAliases, argAliases);

            final var whereStatement = whereExpression != null
                    ? Optional.of(whereExpression.get(executableAggqlyType, argAliasesWithAncestor, Map.of()))
                    : Optional.<String>empty();

            return new SelectNode(executableAggqlyType, selectionNodes, whereStatement, orderBy);
        }

        private AstNode parseJoin(Integer level,
                ExecutableAggqlyType executableAggqlyType,
                Optional<MapWithAncestor<String, String>> previousArgAliases,
                AggqlyJoinImpl aggqlyField,
                Map<String, Object> args,
                GraphQLOutputType rightRawGqlType,
                DataFetchingFieldSelectionSet selectionSet,
                List<Entry<String, String>> orderBy) {

            final var unwrappedGqlType = Parser.MaybeUnwrapGraphQLListType(rightRawGqlType);

            final WhereFunction preparedJoinExpression = aggqlyField.getExpression().reduce(executableAggqlyType);

            final AstNode selectNode = switch (unwrappedGqlType) {
                case GraphQLInterfaceType interfaceType ->
                    this.parseInterface(level, executableAggqlyType, previousArgAliases, interfaceType, args,
                            selectionSet, preparedJoinExpression);
                case GraphQLObjectType objectType ->
                    this.parseSelect(level + 1, Optional.of(executableAggqlyType), previousArgAliases, objectType, args,
                            selectionSet, preparedJoinExpression, List.of());
                default -> new DeathNode("");
            };

            return new JoinNode(aggqlyField.getName(), selectNode, orderBy);
        }

        private AstNode parseJunction(Integer level,
                ExecutableAggqlyType executableAggqlyType,
                Optional<MapWithAncestor<String, String>> previousArgAliases,
                AggqlyJunctionImpl aggqlyField,
                Map<String, Object> args,
                GraphQLOutputType rightRawGqlType,
                DataFetchingFieldSelectionSet selectionSet,
                List<Entry<String, String>> orderBy) {
            final var unwrappedGqlType = Parser.MaybeUnwrapGraphQLListType(rightRawGqlType);

            final WhereFunction preparedJoinExpression = aggqlyField.getExpression().reduce(executableAggqlyType);

            final AstNode selectNode = switch (unwrappedGqlType) {
                case GraphQLInterfaceType interfaceType ->
                    this.parseInterface(level, executableAggqlyType, previousArgAliases, interfaceType, args,
                            selectionSet, preparedJoinExpression);
                case GraphQLObjectType objectType ->
                    this.parseSelect(level + 1, Optional.of(executableAggqlyType), previousArgAliases,
                            objectType, args, selectionSet, preparedJoinExpression, List.of());
                default -> new DeathNode("");
            };

            return new JoinNode(aggqlyField.getName(), selectNode, orderBy);
        };

        private AstNode parseInterface(Integer level,
                ExecutableAggqlyType executableAggqlyType,
                Optional<MapWithAncestor<String, String>> previousArgAliases,
                GraphQLInterfaceType gqlIfType,
                Map<String, Object> args,
                DataFetchingFieldSelectionSet selectionSet,
                WhereFunction whereExpression) {

            final var selectedGqlTypes = selectionSet.getFields()
                    .stream()
                    .map(f -> f.getObjectTypes())
                    .flatMap(List::stream)
                    .collect(Collectors.toUnmodifiableSet());

            if (selectedGqlTypes.size() == 1) {
                return parseSelect(level, Optional.of(executableAggqlyType), previousArgAliases,
                        selectedGqlTypes.stream().findFirst().get(),
                        args, selectionSet, whereExpression, List.of());
            }

            final var selectNodes = selectedGqlTypes
                    .stream()
                    .map(gqlType -> {
                        return parseSelect(level, Optional.of(executableAggqlyType), previousArgAliases,
                                gqlType, args, selectionSet, whereExpression, List.of());
                    })
                    .toList();

            return new InterfaceNode("", selectNodes);
        }

        private AstNode parseComputed(Integer level, AggqlyComputedImpl aggqlyField) {
            return new DeathNode(aggqlyField.getName());
            // return new ColumnNode(aggqlyField.getName(),
            // aggqlyField.getFunction().get(executableTypeAccessor, Map.of(), Map.of()));
        }

        private AstNode parseColumn(Integer level, AggqlyColumnImpl aggqlyField) {
            return new ColumnNode(aggqlyField.getName(), aggqlyField.getColumn());
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
