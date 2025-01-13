package org.femto.aggqly;

import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.femto.aggqly.schema.impl.AggqlyInterfaceScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AggqlyInterfaceScanRegistrar.class)
public @interface AggqlyInterfaceScan {
    String[] value() default {};
}
