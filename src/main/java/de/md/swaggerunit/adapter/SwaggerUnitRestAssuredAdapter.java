package de.md.swaggerunit.adapter;

import de.md.swaggerunit.core.SwaggerUnitCore;
import de.md.swaggerunit.usage.ValidationScope;
import io.restassured.config.HttpClientConfig;
import java.net.URI;
import java.util.*;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.ExecutionContext;
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

	private ValidationScope validationScope = ValidationScope.BOTH;

	private static final Logger LOGGER = LoggerFactory.getLogger(de.md.swaggerunit.adapter.SwaggerUnitSpringAdapter.class);

	public SwaggerUnitRestAssuredAdapter(SwaggerUnitCore unitCore, AbstractHttpClient httpClient) {
		super();
		this.httpClient = httpClient;
		this.unitCore = unitCore;
	}

	public void setValidationScope(ValidationScope validationScope) {
		Objects.requireNonNull(validationScope, "please provide a non null validation scope.");
		this.validationScope = validationScope;
	}

	@Override
	public HttpClient createHttpClient() {
		//Creating an HttpRequestInterceptor
		HttpRequestInterceptor requestInterceptor = (request, context) -> {
			this.request = request;
			requestMethod = request.getRequestLine().getMethod();
			requestUri = URI.create(request.getRequestLine().getUri());

			if (ValidationScope.NONE.equals(validationScope)) {
				LOGGER.warn("Swagger validation is disabled");
			}
			byte[] body = new byte[0];
			if (request instanceof HttpEntityEnclosingRequest) { // TODO check this body consumed?
				HttpEntity reqEntity = ((HttpEntityEnclosingRequest) request).getEntity();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				reqEntity.writeTo(baos);
				body = baos.toByteArray();
			}
			Map<String, List<String>> headers = new HashMap<>();
			for (Header header : request.getAllHeaders()) {
				headers.put(header.getName(), Collections.singletonList(header.getValue()));
			}

			if (validationScope == ValidationScope.REQUEST || validationScope == ValidationScope.BOTH) {
				unitCore.validateRequest(requestMethod, requestUri, headers, new String(body));
			}
		};
		//Creating an HttpRequestInterceptor
		HttpResponseInterceptor responseInterceptor = (response, context) -> {
			if (isJsonResponse(response) && (validationScope == ValidationScope.RESPONSE
					|| validationScope == ValidationScope.BOTH)) {
				HttpHost target = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
				HttpEntity resEntity = new DefaultHttpClient().execute(target, request).getEntity();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				resEntity.writeTo(baos);
				byte[] body = baos.toByteArray();

				Map<String, List<String>> headers = new HashMap<>();
				for (Header header : response.getAllHeaders()) {
					headers.put(header.getName(), Collections.singletonList(header.getValue()));
				}

				unitCore.validateResponse(requestMethod, response.getStatusLine().getStatusCode(), requestUri, headers,
						new String(body));
			}
		};
		httpClient.addRequestInterceptor(requestInterceptor);
		httpClient.addResponseInterceptor(responseInterceptor);
		return httpClient;
	}

	/**
	 * Test if the response body of a response is formatted as json. This function doesn't actually inspect the body, if just
	 * checks if the content-type header contains something like "application/json".
	 *
	 * @param response
	 * @return true if the response body is formatted as json.
	 */
	private boolean isJsonResponse(HttpResponse response) {
		return response.getFirstHeader("content-type").getValue().contains("application/json");
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
