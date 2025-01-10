package org.femto.jaggqlyapp.aggqly;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AggqlyType {
    public String name();

    public String schema() default "";

    public String table();

    public String expression() default "";

    public String[] selectAlways() default {};
}