package org.femto.jaggqlyapp.aggqly.expressions;

import java.util.Optional;
import java.util.function.Function;

public interface SomethingWithAncestor<T extends SomethingWithAncestor<T>> {
    Optional<T> ancestor();

    static <T extends SomethingWithAncestor<T>> Function<T, T> forLookbacks(int n) {
        switch (n) {
            case 0:
                return lookback0();
            case 1:
                return lookback1();
            case 2:
                return lookback2();
            case 3:
                return lookback3();
            default:
                return lookback(n);
        }
    }

    static <T extends SomethingWithAncestor<T>> Function<T, T> lookback0() {
        return root -> root;
    }

    static <T extends SomethingWithAncestor<T>> Function<T, T> lookback1() {
        return root -> root
                .ancestor().orElseThrow();
    }

    static <T extends SomethingWithAncestor<T>> Function<T, T> lookback2() {
        return root -> root
                .ancestor().orElseThrow()
                .ancestor().orElseThrow();
    }

    static <T extends SomethingWithAncestor<T>> Function<T, T> lookback3() {
        return root -> root
                .ancestor().orElseThrow()
                .ancestor().orElseThrow()
                .ancestor().orElseThrow();
    }

    static <T extends SomethingWithAncestor<T>> Function<T, T> lookback(int n) {
        return root -> {
            var current = root;
            for (int i = 0; i < n - 1; i++) {
                current = root.ancestor().orElseThrow();
            }
            return current;
        };
    }
}
