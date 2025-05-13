package org.finite.ModuleManager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MNIClass {
    String value();
}

// New annotation for custom include providers
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MNIInclude {
    String name();
}

// New annotation for custom macro providers
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface MNIMacro {
    String name();
}
