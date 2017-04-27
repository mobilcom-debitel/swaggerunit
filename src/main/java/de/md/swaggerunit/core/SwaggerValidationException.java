package de.md.swaggerunit.core;

/**
 * Created by fpriede on 26.04.2017.
 */
public class SwaggerValidationException extends RuntimeException {

    public SwaggerValidationException(String message) {
        super(message);
    }

    public SwaggerValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
