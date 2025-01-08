package org.femto.jaggqlyapp.aggqly;

import org.femto.jaggqlyapp.aggqly.expressions.ParserException;
import org.femto.jaggqlyapp.aggqly.expressions.WhereFunction;

public final class ComputedField implements AggqlyField {

    public static ComputedField fromAnnotation(String name, AggqlyComputed annotation) throws ParserException {
        final var definingFunction = WhereFunction.fromExpression(annotation.expression());

        return new ComputedField(name, definingFunction);
    }

    public final String name;
    public final WhereFunction function;

    private ComputedField(String name, WhereFunction function) {
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
