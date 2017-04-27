package de.md.swaggerunit.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TestRequestValidation {

	public static final String SWAGGER_DEFINITION = "http://microsrv-d1.mobilcom.de:8016/docs/tariff-swap-service.yaml";

	@Test
	public void testValidation_shouldPass()  {
		SwaggerUnitCore swaggerUnitCore = new SwaggerUnitCore(SWAGGER_DEFINITION);
		URI toTest = URI.create("/v1/contracts/tariffSwap?contractId=mc.123324");

		Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
			put("Channel", Collections.singletonList("App"));
			put("Agent", Collections.singletonList("Fox Mulder"));
		}};

		swaggerUnitCore.validateRequest("GET", toTest, headers, null);
	}

	@Test
	public void oneHeaderMissing_shouldFail() throws JsonProcessingException {
		try {
			SwaggerUnitCore swaggerUnitCore = new SwaggerUnitCore(SWAGGER_DEFINITION);
			URI toTest = URI.create("/v1/contracts/tariffSwap?contractId=mc.123324");

			Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
				put("Channel", Collections.singletonList("App"));
			}};

			swaggerUnitCore.validateRequest("GET", toTest, headers, null);
			fail("test should have failed!");
		}
		catch(SwaggerValidationException ex){
			assertNotNull(ex);
			assertNotNull(ex.getMessage());
			assertEquals("error message is unexpected!", "Mandatory header \"Agent\" is not set.", ex.getMessage());
		}
	}

	@Test
	public void allHeaderMissing_shouldFail() throws JsonProcessingException {
		try {
			SwaggerUnitCore swaggerUnitCore = new SwaggerUnitCore(SWAGGER_DEFINITION);
			URI toTest = URI.create("/v1/contracts/tariffSwap?contractId=mc.123324");

			Map<String, List<String>> headers = new HashMap<>();

			swaggerUnitCore.validateRequest("GET", toTest, headers, null);
			fail("test should have failed!");
		}
		catch(SwaggerValidationException ex){
			assertNotNull(ex);
			assertNotNull(ex.getMessage());
			assertTrue(ex.getMessage().contains("Mandatory header \"Agent\" is not set."));
			assertTrue(ex.getMessage().contains("Mandatory header \"Channel\" is not set."));
		}
	}

	@Test
	public void queryParamIsMissing_shouldFail(){
		try {
			SwaggerUnitCore swaggerUnitCore = new SwaggerUnitCore(SWAGGER_DEFINITION);
			URI toTest = URI.create("/v1/contracts/tariffSwap");

			Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
				put("Channel", Collections.singletonList("App"));
				put("Agent", Collections.singletonList("Fox Mulder"));
			}};

			swaggerUnitCore.validateRequest("GET", toTest, headers, null);
			fail("test should have failed!");
		}
		catch(SwaggerValidationException ex){
			assertNotNull(ex);
			assertNotNull(ex.getMessage());
			assertTrue(ex.getMessage().contains("Query parameter 'contractId' is required on path '/contracts/tariffSwap' but not found in request."));
		}
	}

	@Test
	public void shouldIgnoreAdditionalHeaders_shouldPass(){
		SwaggerUnitCore swaggerUnitCore = new SwaggerUnitCore(SWAGGER_DEFINITION);
		URI toTest = URI.create("/v1/contracts/tariffSwap?contractId=mc.123324");

		Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
			put("Channel", Collections.singletonList("App"));
			put("Agent", Collections.singletonList("Fox Mulder"));
			put("Something", Collections.singletonList("Somewhere"));
		}};

		swaggerUnitCore.validateRequest("GET", toTest, headers, null);
	}
}
