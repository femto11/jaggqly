package org.femto.aggqly.schema.impl;

import org.femto.aggqly.schema.AggqlyColumn;

public final class AggqlyColumnImpl implements AggqlyField {

    public static AggqlyColumnImpl fromName(String name) {
        // return new ColumnField(name, (t, args, ctx) -> t + "." + name);
        return new AggqlyColumnImpl(name, name);
    }

    public static AggqlyColumnImpl fromAnnotation(String name, AggqlyColumn annotation) {
        return annotation.column().isEmpty()
                ? new AggqlyColumnImpl(name, name)
                : new AggqlyColumnImpl(name, annotation.column());
    }

    // @SuppressWarnings("unused")
    // public static ColumnField fromAnnotation(String name, AggqlyColumn
    // annotation) {
    // WhereFunction definingFunction = annotation.expression().isEmpty()
    // ? annotation.column().isEmpty()
    // ? (t, args, ctx) -> t + "." + name
    // : (t, args, ctx) -> t + "." + annotation.column()
    // : WhereFunction.fromExpression(annotation.expression());

    // return new ColumnField(name, definingFunction);
    // }

    public final String name;
    public final String column;

    private AggqlyColumnImpl(String name, String column) {
        this.name = name;
        this.column = column;
    }

    public String getName() {
        return this.name;
    }

    public String getColumn() {
        return this.column;
    }
}
