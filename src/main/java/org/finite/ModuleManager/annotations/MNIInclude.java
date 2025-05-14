package org.finite.ModuleManager.annotations;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
// New annotation for custom include providers
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MNIInclude {
    String name();
}
