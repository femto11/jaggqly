package org.femto.jaggqlyapp.aggqly;

import java.util.Optional;

import org.femto.jaggqlyapp.aggqly.impl.WhereExpressionImpl;

public final class RootField implements AggqlyField {
    public static RootField fromName(String name) {
        return new RootField(name, null);
    }

    public static RootField fromAnnotation(String name, AggqlyRoot annotation) {
        final var where = !annotation.where().isEmpty()
                ? new WhereExpressionImpl(annotation.where())
                : null;

        return new RootField(name, where);
    }

    public final String name;
    public final WhereExpression where;

    private RootField(String name, WhereExpression where) {
        this.name = name;
        this.where = where;
    }

    public String getName() {
        return this.name;
    }

    public WhereExpression getWhere() {
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

        public RootField build() {
            return new RootField(name, null);
        }
    }
}
