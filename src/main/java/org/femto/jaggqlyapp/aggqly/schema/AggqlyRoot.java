package org.femto.jaggqlyapp.aggqly.schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AggqlyRoot {
    public String name() default "";

    public String where() default "";
}