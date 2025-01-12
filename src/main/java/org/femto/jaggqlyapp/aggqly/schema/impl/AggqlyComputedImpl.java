package org.femto.jaggqlyapp.aggqly.schema.impl;

import org.femto.jaggqlyapp.aggqly.expressions.ParserException;
import org.femto.jaggqlyapp.aggqly.expressions.WhereFunction;
import org.femto.jaggqlyapp.aggqly.schema.AggqlyComputed;

public final class AggqlyComputedImpl implements AggqlyField {

    public static AggqlyComputedImpl fromAnnotation(String name, AggqlyComputed annotation) throws ParserException {
        final var definingFunction = WhereFunction.fromExpression(annotation.expression());

        return new AggqlyComputedImpl(name, definingFunction);
    }

    public final String name;
    public final WhereFunction function;

    private AggqlyComputedImpl(String name, WhereFunction function) {
        this.name = name;
        this.function = function;
    }

    public String getName() {
        return this.name;
    }

    public WhereFunction getFunction() {
        return this.function;
    }
}
