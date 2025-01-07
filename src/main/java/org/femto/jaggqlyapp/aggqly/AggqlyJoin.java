package org.femto.jaggqlyapp.aggqly;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AggqlyJoin {
    public String expression();

    public String orderByArg() default "orderBy";
}