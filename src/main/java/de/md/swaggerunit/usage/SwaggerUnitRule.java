package de.md.swaggerunit.usage;

import java.util.Arrays;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import de.md.swaggerunit.adapter.SwaggerUnitSpringAdapter;
import de.md.swaggerunit.core.SwaggerUnitCore;

public class SwaggerUnitRule implements MethodRule {

	private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerUnitRule.class);
	private SwaggerUnitCore swaggerUnit;
	private RestTemplate restTemplate;
	private SwaggerUnitSpringAdapter swaggerUnitInterceptor;

	public SwaggerUnitRule(String swaggerUri, RestTemplate restTemplate) {
		super();
		this.swaggerUnit = new SwaggerUnitCore(swaggerUri);
		this.restTemplate = restTemplate;
		this.swaggerUnitInterceptor = new SwaggerUnitSpringAdapter(swaggerUnit);
		restTemplate.setInterceptors(Arrays.asList(swaggerUnitInterceptor));
	}

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				SwaggerValidation annotation = method.getAnnotation(SwaggerValidation.class);
				if(annotation != null) {
					swaggerUnitInterceptor.setValidationScope(annotation.value());

					try {
						base.evaluate();
					} catch (Throwable e) {
						LOGGER.error("Beim ausführen eines Tests ist eine Exception aufgetreten: {}", e);
						throw new RuntimeException(e);
					}
					swaggerUnitInterceptor.setValidationScope(ValidationScope.NONE);
				}
			}
		};
	}

}
