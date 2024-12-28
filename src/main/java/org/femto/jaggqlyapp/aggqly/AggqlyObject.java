package org.femto.jaggqlyapp.aggqly;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class AggqlyObject {
    public final String typeName;
    public final String tableName;
    public final Optional<String[]> selectAlwaysNames;
    public final Optional<WhereExpression> whereExpression;
    public final Map<String, AggqlyField> fields;

    private AggqlyObject(
            String typeName,
            String tableName,
            Optional<String[]> selectAlwaysNames,
            Optional<WhereExpression> whereExpression,
            final Map<String, AggqlyField> fields) {
        this.typeName = typeName;
        this.tableName = tableName;
        this.selectAlwaysNames = selectAlwaysNames;
        this.whereExpression = whereExpression;
        this.fields = fields;
    }

    public String getTable() {
        return this.tableName;
    }

    public AggqlyField getField(String name) {
        return this.fields.get(name);
    }

    public static class Builder {

        private String typeName;
        private String tableName;
        private Optional<String[]> selectAlwaysNames;
        private Optional<WhereExpression> whereExpression;
        private final Map<String, AggqlyField> fields;

        public Builder(final String typeName) {
            this.typeName = typeName;
            this.fields = new HashMap<>();
        }

        public Builder table(final String name) {
            this.tableName = name;
            return this;
        }

        public Builder selectAlways(final String[] names) {
            this.selectAlwaysNames = Optional.of(names);
            return this;
        }

        public Builder where(WhereExpression expression) {
            this.whereExpression = Optional.of(expression);
            return this;
        }

        public Builder field(AggqlyField field) {
            this.fields.put(field.getName(), field);
            return this;
        }

        public AggqlyObject build() {
            return new AggqlyObject(typeName, tableName, selectAlwaysNames, whereExpression, fields);
        }
    }
}