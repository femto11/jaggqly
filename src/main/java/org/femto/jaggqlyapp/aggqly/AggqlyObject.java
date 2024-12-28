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
    private final Map<String, RootField> roots;

    private AggqlyObject(
            String typeName,
            String tableName,
            Optional<String[]> selectAlwaysNames,
            Optional<WhereExpression> whereExpression,
            final Map<String, AggqlyField> fields,
            final Map<String, RootField> roots) {
        this.typeName = typeName;
        this.tableName = tableName;
        this.selectAlwaysNames = selectAlwaysNames;
        this.whereExpression = whereExpression;
        this.fields = fields;
        this.roots = roots;
    }

    public String getTable() {
        return this.tableName;
    }

    public AggqlyField getField(String name) {
        return this.fields.get(name);
    }

    public RootField getRoot(String name) {
        return this.roots.get(name);
    }

    public static class Builder {

        private String typeName;
        private String tableName;
        private Optional<String[]> selectAlwaysNames;
        private Optional<WhereExpression> whereExpression;
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

        public Builder root(RootField field) {
            this.roots.put(field.getName(), field);
            return this;
        }

        public AggqlyObject build() {
            return new AggqlyObject(typeName, tableName, selectAlwaysNames, whereExpression, fields, roots);
        }
    }
}