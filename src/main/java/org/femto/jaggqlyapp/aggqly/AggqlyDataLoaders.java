package org.femto.jaggqlyapp.aggqly;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public final class AggqlyDataLoaders {

    private Map<String, AggqlyObject> types;

    public AggqlyDataLoaders(Map<String, AggqlyObject> types) {
        this.types = types;
    }

    public Map<String, AggqlyObject> getTypes() {
        return this.types;
    }

    public AggqlyObject getType(@NotNull String name) {
        return this.types.get(name);
    }

    public static class Builder {

        private HashMap<String, AggqlyObject> types;

        public Builder() {
            types = new HashMap<>();
        }

        public Builder type(AggqlyObject type) {
            types.put(type.typeName, type);
            return this;
        }

        public AggqlyDataLoaders build() {
            return new AggqlyDataLoaders(Collections.unmodifiableMap(this.types));
        }
    }
}
