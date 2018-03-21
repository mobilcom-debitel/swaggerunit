package de.md.swaggerunit.core;

/**
 * Created by fpriede on 26.04.2017.
 */
public class SwaggerValidationException extends RuntimeException {

	private static final long serialVersionUID = 7185046285868351986L;

	public SwaggerValidationException(String message) {
		super(message);
	}

	public SwaggerValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
