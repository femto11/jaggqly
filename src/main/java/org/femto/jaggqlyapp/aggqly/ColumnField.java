package org.femto.jaggqlyapp.aggqly;

public final class ColumnField implements AggqlyField {

    public static ColumnField fromName(String name) {
        // return new ColumnField(name, (t, args, ctx) -> t + "." + name);
        return new ColumnField(name, name);
    }

    public static ColumnField fromAnnotation(String name, AggqlyColumn annotation) {
        return annotation.column().isEmpty()
                ? new ColumnField(name, name)
                : new ColumnField(name, annotation.column());
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

    private ColumnField(String name, String column) {
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
