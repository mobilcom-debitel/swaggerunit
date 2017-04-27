package de.md.swaggerunit.adapter;

import de.md.swaggerunit.usage.ValidationScope;

public interface SwaggerUnitAdapter {

	public void validate(ValidationScope validationScope);
	
	public void afterValidation();
}
