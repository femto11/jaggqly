package org.femto.aggqly.schema.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public final class AggqlyDataLoaders {

    private Map<String, AggqlyTypeImpl> types;

    public AggqlyDataLoaders(Map<String, AggqlyTypeImpl> types) {
        this.types = types;
    }

    public Map<String, AggqlyTypeImpl> getTypes() {
        return this.types;
    }

    public AggqlyTypeImpl getType(@NotNull String name) {
        return this.types.get(name);
    }

    public static class Builder {

        private HashMap<String, AggqlyTypeImpl> types;

        public Builder() {
            types = new HashMap<>();
        }

        public Builder type(AggqlyTypeImpl type) {
            types.put(type.typeName, type);
            return this;
        }

        public AggqlyDataLoaders build() {
            return new AggqlyDataLoaders(Collections.unmodifiableMap(this.types));
        }
    }
}
