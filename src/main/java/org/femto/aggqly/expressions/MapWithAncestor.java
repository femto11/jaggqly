package org.femto.aggqly.expressions;

import java.util.Map;
import java.util.Optional;

public record MapWithAncestor<K, V>(Optional<MapWithAncestor<K, V>> ancestor, Map<K, V> items)
        implements SomethingWithAncestor<MapWithAncestor<K, V>> {

    public V get(K k) {
        return items.get(k);
    }
}