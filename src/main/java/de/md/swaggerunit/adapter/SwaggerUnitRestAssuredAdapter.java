package de.md.swaggerunit.adapter;

import de.md.swaggerunit.core.SwaggerUnitCore;
import static de.md.swaggerunit.core.SwaggerUnitCore.SKIP_VALIDATION_KEY;
import static de.md.swaggerunit.core.SwaggerUnitCore.SKIP_VALIDATION_VALUE;
import de.md.swaggerunit.usage.ValidationScope;
import io.restassured.config.HttpClientConfig;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mlipka
 * @since 25.11.2019
 */
public class SwaggerUnitRestAssuredAdapter implements HttpClientConfig.HttpClientFactory, SwaggerUnitAdapter {

	private SwaggerUnitCore unitCore;

	private String requestMethod;
	private URI requestUri;
	private AbstractHttpClient httpClient;
	private HttpRequest request;

	private ValidationScope validationScope = ValidationScope.NONE;
	private static final Logger LOGGER = LoggerFactory.getLogger(de.md.swaggerunit.adapter.SwaggerUnitSpringAdapter.class);

	public SwaggerUnitRestAssuredAdapter(SwaggerUnitCore unitCore, AbstractHttpClient httpClient) {
		super();
		this.httpClient = httpClient;
		this.unitCore = unitCore;
	}

	@Override
	public HttpClient createHttpClient() {
		if (SKIP_VALIDATION_VALUE.equalsIgnoreCase(System.getProperty(SKIP_VALIDATION_KEY))) {
			LOGGER.warn("Swagger validation is disabled");
		} else {
			httpClient.addRequestInterceptor((request, context) -> validateRequestInterceptor(request));
			httpClient.addResponseInterceptor(this::validateResponseInterceptor);
		}
		return httpClient;
	}

	/**
	 * Test if the response body of a response is formatted as json. This function doesn't actually inspect the body, if just
	 * checks if the content-type header contains something like "application/json".
	 *
	 * @param response -
	 * @return true if the response body is formatted as json.
	 */
	private boolean isJsonResponse(HttpResponse response) {
		return response.getFirstHeader("content-type") != null && response.getFirstHeader("content-type")
				.getValue()
				.contains("application/json");
	}

	/**
	 * Validate the incoming request.
	 *
	 * @param request -
	 * @throws IOException -
	 */
	private void validateRequestInterceptor(HttpRequest request) throws IOException {
		if (ValidationScope.NONE.equals(validationScope)) {
			LOGGER.warn("Swagger validation is disabled");
		} else {
			this.request = request;
			requestMethod = request.getRequestLine()
					.getMethod();
			requestUri = URI.create(request.getRequestLine()
					.getUri());

			byte[] body = new byte[0];
			if (request instanceof HttpEntityEnclosingRequest) {
				HttpEntity reqEntity = ((HttpEntityEnclosingRequest) request).getEntity();
				if (reqEntity != null) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					reqEntity.writeTo(baos);
					body = baos.toByteArray();
				}
			}
			Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			for (Header header : request.getAllHeaders()) {
				headers.put(header.getName(), Collections.singletonList(header.getValue()));
			}

			if (validationScope == ValidationScope.REQUEST || validationScope == ValidationScope.BOTH) {
				unitCore.validateRequest(requestMethod, requestUri, headers, new String(body));
			}
		}
	}

	/**
	 * Validate outgoing response.
	 *
	 * @param response -
	 * @param context Context is needed to resend the same request as the response body needs to be evaluated
	 * @throws IOException -
	 */
	private void validateResponseInterceptor(HttpResponse response, HttpContext context) throws IOException {
		if (isJsonResponse(response) && (validationScope == ValidationScope.RESPONSE
				|| validationScope == ValidationScope.BOTH)) {
			HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			request.removeHeaders(HTTP.CONTENT_LEN);
			HttpEntity resEntity = new DefaultHttpClient().execute(target, request)
					.getEntity();
			byte[] body = new byte[0];
			if(resEntity != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resEntity.writeTo(baos);
				body = baos.toByteArray();
			}

			Map<String, List<String>> headers = new HashMap<>();
			for (Header header : response.getAllHeaders()) {
				headers.put(header.getName(), Collections.singletonList(header.getValue()));
			}

			unitCore.validateResponse(requestMethod, response.getStatusLine()
					.getStatusCode(), requestUri, headers, new String(body));
		}
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
