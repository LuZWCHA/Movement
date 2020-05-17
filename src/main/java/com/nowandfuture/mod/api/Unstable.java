package com.nowandfuture.mod.api;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

/**
 * This annotation is used to annotate the Class,Method or Value that are not stable(may change soon).
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(value={CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE})
public @interface Unstable {
    String description() default "";
}
