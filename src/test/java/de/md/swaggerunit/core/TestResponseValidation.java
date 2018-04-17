package de.md.swaggerunit.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

/**
 * Created by fpriede on 26.04.2017.
 */
public class TestResponseValidation {

	private SwaggerUnitCore core = new SwaggerUnitCore(TestRequestValidation.SWAGGER_DEFINITION);

	@Test(expected = SwaggerValidationException.class)
	public void test() throws URISyntaxException {
			String responseBody = "{\n" +
					"  \"tariffs\": [\n" +
					"		{\n" +
					"			\"idd\": \"213124\",\n" +
					"			\"label\": \"RED 4 GB mit Handy 10\",\n" +
					"			\"priceRecurrent\": \"39.99\",\n" +
					"			\"fee\": \"19.99\",\n" +
					"			\"date\": \"2017-04-26T08:33:22.782Z\",\n" +
					"			\"netCode\": \"D1\",\n" +
					"			\"mustProlongate\": true\n" +
					"		}\n" +
					"	]\n" +
					"}";

		Map<String, List<String>> headers = new HashMap<>();

		core.validateResponse("GET", 200, URI.create("/contracts/tariffSwap"), headers, responseBody);
	}

}
