package de.md.swaggerunit.adapter;

import de.md.swaggerunit.core.SwaggerUnitCore;
import de.md.swaggerunit.usage.ValidationScope;
import java.io.IOException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SwaggerUnitSpringAdapter implements ClientHttpRequestInterceptor, SwaggerUnitAdapter {

	private SwaggerUnitCore unitCore;

	private ValidationScope validationScope = ValidationScope.NONE;

	private static final String SKIP_VALIDATION_VALUE = "true";
	private static final String SKIP_VALIDATION_KEY = "swaggerunit.validation.skip";
	private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerUnitSpringAdapter.class);

	public SwaggerUnitSpringAdapter(SwaggerUnitCore unitCore, RestTemplate swaggerUnitHttpClient) {
		super();
		swaggerUnitHttpClient.setInterceptors(Arrays.asList(this));
		this.unitCore = unitCore;
	}

	/**
	 * Test if the response body of a response is formatted as json. This function doesn't actually inspect the body, if just
	 * checks if the content-type header contains something like "application/json".
	 *
	 * @param response -
	 * @return true if the response body is formatted as json.
	 */
	private boolean isJsonResponse(ClientHttpResponse response) {
		return response.getHeaders().getContentType() != null && response.getHeaders().getContentType()
				.isCompatibleWith(MediaType.APPLICATION_JSON);
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {

		if (ValidationScope.NONE.equals(validationScope) || SKIP_VALIDATION_VALUE
				.equalsIgnoreCase(System.getProperty(SKIP_VALIDATION_KEY))) {
			LOGGER.warn("Swagger validation is disabled");
		} else {
			unitCore.validateRequest(request.getMethod().name(), request.getURI(), request.getHeaders(), new String(body));
		}
		ClientHttpResponse response = execution.execute(request, body);
		if (isJsonResponse(response) && (validationScope == ValidationScope.RESPONSE
				|| validationScope == ValidationScope.BOTH)) {
			ClonedHttpResponse clonedHttpResponse = ClonedHttpResponse.createFrom(response);
			unitCore.validateResponse(request.getMethod().name(), response.getRawStatusCode(), request.getURI(),
					request.getHeaders(), new String(clonedHttpResponse.getRawBody()));
			return clonedHttpResponse;
		}
		return response;
	}

	@Override
	public void afterValidation() {
		this.validationScope = ValidationScope.NONE;
	}

	@Override
	public void validate(ValidationScope validationScope) {
		this.validationScope = validationScope;
	}

}
