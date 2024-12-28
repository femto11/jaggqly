package org.femto.jaggqlyapp.aggqly;

import java.util.Optional;

public final class ColumnField implements AggqlyField {
    public static ColumnField fromName(String name) {
        return new ColumnField(name, name);
    }

    public static ColumnField fromAnnotation(AggqlyColumn annotation) {
        return new ColumnField("Foo", "Bar");
    }

    public final String name;
    public final String column;

    private ColumnField(String name, String column) {
        this.name = name;
        this.column = column;
    }

    public String getName() {
        return this.name;
    }

    public String getColumn() {
        return column;
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

        public ColumnField build() {
            return new ColumnField(name, columnName.orElse(name));
        }
    }
}
