package de.md.swaggerunit.usage;

/**
 * Says the validtion scope for the swagger-validation.
 * 
 * @author dgoermann, fpriede
 * @version 1.0
 */
public enum ValidationScope {
	/**
	 * No validation will be done
	 */
	NONE,
	/**
	 * The request and the response will be validated
	 */
	BOTH,
	/**
	 * Only the request will be validated
	 */
	REQUEST,
	/**
	 * Only the response will be validated
	 */
	RESPONSE
}
