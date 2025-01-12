package org.femto.jaggqlyapp.aggqly.schema.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.femto.jaggqlyapp.aggqly.expressions.ParserException;
import org.femto.jaggqlyapp.aggqly.expressions.WhereFunction;
import org.femto.jaggqlyapp.aggqly.schema.AggqlyType;

final class AggqlyTypeImpl {
    public final String typeName;
    public final String tableName;
    public final Optional<WhereFunction> expression;
    public final Optional<String[]> selectAlwaysNames;
    public final Map<String, AggqlyField> fields;
    private final Map<String, AggqlyRootImpl> roots;
    private Optional<String> schemaName;

    private AggqlyTypeImpl(
            String typeName,
            Optional<String> schemaName,
            String tableName,
            Optional<WhereFunction> expression,
            final Map<String, AggqlyField> fields,
            final Map<String, AggqlyRootImpl> roots,
            Optional<String[]> selectAlwaysNames) {
        this.typeName = typeName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.expression = expression;
        this.selectAlwaysNames = selectAlwaysNames;
        this.fields = fields;
        this.roots = roots;
    }

    public Optional<String> getSchema() {
        return this.schemaName;
    }

    public String getTable() {
        return this.tableName;
    }

    public Optional<WhereFunction> getExpression() {
        return this.expression;
    }

    public AggqlyField getField(String name) {
        return this.fields.get(name);
    }

    public AggqlyRootImpl getRoot(String name) {
        return this.roots.get(name);
    }

    public static class Builder {
        static Builder fromAnnotation(AggqlyType annotation) {
            return new Builder(annotation.name())
                    .schema(annotation.schema())
                    .table(annotation.table())
                    .expression(annotation.expression())
                    .selectAlways(annotation.selectAlways());
        }

        private String typeName;
        private String tableName;
        private String schemaName;
        private Optional<String[]> selectAlwaysNames;
        private String expression;
        private final Map<String, AggqlyField> fields;
        private final Map<String, AggqlyRootImpl> roots;

        public Builder(final String typeName) {
            this.typeName = typeName;
            this.fields = new HashMap<>();
            this.roots = new HashMap<>();
        }

        public Builder schema(final String schema) {
            this.schemaName = schema;
            return this;
        }

        public Builder table(final String name) {
            this.tableName = name;
            return this;
        }

        public Builder expression(String expression) {
            this.expression = expression;
            return this;
        }

        public Builder selectAlways(final String[] names) {
            this.selectAlwaysNames = Optional.of(names);
            return this;
        }

        public Builder field(AggqlyField field) {
            this.fields.put(field.getName(), field);
            return this;
        }

        public Builder root(AggqlyRootImpl field) {
            this.roots.put(field.getName(), field);
            return this;
        }

        public AggqlyTypeImpl build() throws ParserException {
            if (this.expression.isEmpty()) {
                return new AggqlyTypeImpl(typeName,
                        this.schemaName.isEmpty() ? Optional.empty() : Optional.of(this.schemaName), this.tableName,
                        Optional.empty(), fields, roots,
                        selectAlwaysNames);
            }

            final var function = WhereFunction.fromExpression(this.expression);
            WhereFunction thunk = (a, b, c) -> "(" + function.get(a, b, c) + ")";
            return new AggqlyTypeImpl(typeName, Optional.empty(), this.tableName, Optional.of(thunk), fields, roots,
                    selectAlwaysNames);
        }
    }
}