package org.femto.jaggqlyapp.aggqly;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.femto.jaggqlyapp.aggqly.expressions.ParserException;
import org.femto.jaggqlyapp.aggqly.expressions.WhereFunction;

public final class AggqlyObject {
    public final String typeName;
    public final String tableName;
    public final Optional<WhereFunction> expression;
    public final Optional<String[]> selectAlwaysNames;
    public final Map<String, AggqlyField> fields;
    private final Map<String, RootField> roots;

    private AggqlyObject(
            String typeName,
            String tableName,
            Optional<WhereFunction> expression,
            final Map<String, AggqlyField> fields,
            final Map<String, RootField> roots,
            Optional<String[]> selectAlwaysNames) {
        this.typeName = typeName;
        this.tableName = tableName;
        this.expression = expression;
        this.selectAlwaysNames = selectAlwaysNames;
        this.fields = fields;
        this.roots = roots;
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

    public RootField getRoot(String name) {
        return this.roots.get(name);
    }

    public static class Builder {
        static Builder fromAnnotation(AggqlyType annotation) {
            return new Builder(annotation.name())
                    .table(annotation.table())
                    .expression(annotation.expression())
                    .selectAlways(annotation.selectAlways());
        }

        private String typeName;
        private String tableName;
        private Optional<String[]> selectAlwaysNames;
        private String expression;
        private final Map<String, AggqlyField> fields;
        private final Map<String, RootField> roots;

        public Builder(final String typeName) {
            this.typeName = typeName;
            this.fields = new HashMap<>();
            this.roots = new HashMap<>();
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

        public Builder root(RootField field) {
            this.roots.put(field.getName(), field);
            return this;
        }

        public AggqlyObject build() throws ParserException {
            if (this.expression.isEmpty()) {
                return new AggqlyObject(typeName, this.tableName, Optional.empty(), fields, roots, selectAlwaysNames);
            }

            final var function = WhereFunction.fromExpression(this.expression);
            WhereFunction thunk = (a, b, c) -> "(" + function.get(a, b, c) + ")";
            return new AggqlyObject(typeName, this.tableName, Optional.of(thunk), fields, roots, selectAlwaysNames);
        }
    }
}