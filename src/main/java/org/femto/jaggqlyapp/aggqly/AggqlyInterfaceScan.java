package org.femto.jaggqlyapp.aggqly;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AggqlyInterfaceScanRegistrar.class)
public @interface AggqlyInterfaceScan {
    String[] value() default {};
}
