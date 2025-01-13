package org.femto.aggqly.schema.impl;

import java.util.Optional;

import org.femto.aggqly.expressions.WhereFunction;
import org.femto.aggqly.schema.AggqlyRoot;

final class AggqlyRootImpl implements AggqlyField {
    public static AggqlyRootImpl fromName(String name) {
        return new AggqlyRootImpl(name, null);
    }

    public static AggqlyRootImpl fromAnnotation(String name, AggqlyRoot annotation) {
        final var where = !annotation.where().isEmpty()
                ? WhereFunction.fromExpression(annotation.where())
                : null;

        return new AggqlyRootImpl(name, where);
    }

    public final String name;
    public final WhereFunction where;

    private AggqlyRootImpl(String name, WhereFunction where) {
        this.name = name;
        this.where = where;
    }

    public String getName() {
        return this.name;
    }

    public WhereFunction getWhere() {
        return this.where;
    }

    public static class Builder {
        private String name;
        private Optional<String> columnName;

        public Builder(String name) {
            this.name = name;
            this.columnName = Optional.empty();
        }

        public Builder column(String name) {
            this.columnName = Optional.of(name);
            return this;
        }

        public AggqlyRootImpl build() {
            return new AggqlyRootImpl(name, null);
        }
    }
}
