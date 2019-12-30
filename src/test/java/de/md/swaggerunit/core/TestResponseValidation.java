package de.md.swaggerunit.core;

import com.atlassian.oai.validator.SwaggerRequestResponseValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import mockit.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Created by fpriede on 26.04.2017.
 */
public class TestResponseValidation {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Injectable
	SwaggerUnitConfiguration swaggerUnitConfiguration;

	@Injectable
	SwaggerAuthentication swaggerAuthentication;

	@Tested(fullyInitialized = true)
	SwaggerUnitCore swaggerUnitCore;

	@Before
	public void satisfyConstructionRequirements() {
		new Expectations() {
			{
				swaggerUnitConfiguration.getSwaggerSourceOverride();
				result = TestRequestValidation.SWAGGER_DEFINITION2;
				swaggerAuthentication.getAuth();
				result = Optional.empty();
			}
		};
	}

	/**
	 * Path was not found, so no validation took place.
	 */
	@Test
	public void testURINotInSwagger() {
		Map<String, List<String>> headers = new HashMap<>();

		swaggerUnitCore.validateResponse("GET", 200, URI.create("/different/uri"), headers, null);
	}

	/**
	 * Validation failed since empty Response body is not allowed.
	 */
	@Test(expected = SwaggerValidationException.class)
	public void testMissingResponseBody() {
		String responseBody = "{}";

		Map<String, List<String>> headers = new HashMap<>();

		swaggerUnitCore.validateResponse("GET", 200, URI.create("/v1/contracts/reactivation/MC.12345/check"), headers, responseBody);
	}

	@Test
	public void testValid() {
		String responseBody = "{\"valid\": true}";

		Map<String, List<String>> headers = new HashMap<>();

		swaggerUnitCore.validateResponse("GET", 200, URI.create("/v1/contracts/reactivation/MC.12345/check"), headers, responseBody);
	}

	/**
	 * Path found but apiOperation not allowed, so no validation took place.
	 */
	@Test
	public void testMethodNotInSwagger() {
		Map<String, List<String>> headers = new HashMap<>();

		swaggerUnitCore.validateResponse("PUT", 200, URI.create("/v1/contracts/reactivation/MC.12345/check"), headers, null);
	}

}
