package de.md.swaggerunit.usage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface SwaggerValidation {

	ValidationScope value() default ValidationScope.BOTH;
}
