package de.md.swaggerunit.usage;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.md.swaggerunit.adapter.SwaggerUnitAdapter;

public class SwaggerUnitRule implements MethodRule {

	private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerUnitRule.class);
	private SwaggerUnitAdapter adapter;

	public SwaggerUnitRule(SwaggerUnitAdapter adapter) {
		super();
		this.adapter = adapter;
	}

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				SwaggerValidation annotation = method.getAnnotation(SwaggerValidation.class);
				if (annotation != null) {
					adapter.validate(annotation.value());
					try {
						base.evaluate();
					} catch (Throwable e) {
						LOGGER.error("Beim ausführen eines Tests ist eine Exception aufgetreten: {}", e);
						throw new RuntimeException(e);
					}
					adapter.afterValidation();
				}
			}
		};
	}

}
