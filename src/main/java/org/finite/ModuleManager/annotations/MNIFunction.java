package org.finite.ModuleManager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to mark a method as a function in the MNIModule.
 * This annotation is used to specify the module and function name for the method.
 *
 * this provides a way to register functions in the MNIModule system.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MNIFunction {
    String module();
    String name();
}
