package de.md.swaggerunit.adapter;

import de.md.swaggerunit.usage.SwaggerUnitRule;
import de.md.swaggerunit.usage.ValidationScope;

/**
 * this interface could be used to connect a http client (used in your junit test) with the {@link SwaggerUnitRule}. The rule
 * calles firstly the validate method and after the test is finished the afterValidation method.
 * 
 * @author dgoermann
 */
public interface SwaggerUnitAdapter {

	/**
	 * Validates the request with the given scopes.
	 * 
	 * @param validationScope
	 */
	public void validate(ValidationScope validationScope);

	/**
	 * Will be called after the junit test is finished
	 */
	public void afterValidation();
}
