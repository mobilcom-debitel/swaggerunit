package de.md.swaggerunit.core;

import com.atlassian.oai.validator.SwaggerRequestResponseValidator;
import com.atlassian.oai.validator.SwaggerRequestResponseValidator.Builder;
import com.atlassian.oai.validator.interaction.ApiOperationResolver;
import com.atlassian.oai.validator.model.ApiOperationMatch;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.report.ValidationReport.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Swagger;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class SwaggerUnitCore {

	public static final String SKIP_VALIDATION_VALUE = "true";
	public static final String SKIP_VALIDATION_KEY = "swaggerunit.validation.skip";
	private static final String STRICT_VALIDATION_KEY = "swaggerunit.validation.strict";
	private static final String STRICT_VALIDATION_VALUE = "true";

	private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerUnitCore.class);

	private SwaggerAuthentication authentication;

	private SwaggerUnitConfiguration swaggerUnitConfiguration;

	private SwaggerRequestResponseValidator validator;

	private Swagger swagger;

	/**
	 * Initialisiert SwaggerUnitCore und verwendet swaggerDefinition als das für die Validierung maßgebliche Swagger.
	 *
	 * @param swaggerDefinition eine Plaintext-Swagger-Definition.
	 */
	@Deprecated
	public SwaggerUnitCore(String swaggerDefinition) {
		init(swaggerDefinition, Optional.empty());
	}

	public SwaggerUnitCore(SwaggerUnitConfiguration swaggerUnitConfiguration) {
		this.swaggerUnitConfiguration = swaggerUnitConfiguration;
		this.authentication = new SwaggerAuthentication(new RestTemplate(), swaggerUnitConfiguration);
		init();
	}

	@Inject
	public SwaggerUnitCore(SwaggerUnitConfiguration swaggerUnitConfiguration, SwaggerAuthentication authentication) {
		this.swaggerUnitConfiguration = swaggerUnitConfiguration;
		this.authentication = authentication;
		init();
	}

	/**
	 * Ignoriere SwaggerUnit bei Exceptions es sei den dies wurde per VM Parameter überschrieben.
	 */
	private void init() {
		try {
			init(swaggerUnitConfiguration.getSwaggerSourceOverride(), authentication.getAuth());
		} catch (Exception ex) {
			if (ex instanceof RestClientException) {
				LOGGER.error("Die URL <{}> hat mit einem Fehler geantwortet.", swaggerUnitConfiguration.getSwaggerLoginUrl());
			} else {
				LOGGER.error("Die Swagger <{}> konnte nicht initializiert werden.",
						swaggerUnitConfiguration.getSwaggerSourceOverride());
			}
			if (STRICT_VALIDATION_VALUE.equalsIgnoreCase(System.getProperty(STRICT_VALIDATION_KEY))) {
				throw ex;
			}
			System.setProperty(SKIP_VALIDATION_KEY, SKIP_VALIDATION_VALUE);
		}
	}

	private void init(String swaggerUriOrFileContents, Optional<AuthorizationValue> auth) {
		initValidator(swaggerUriOrFileContents, auth);
		initSwagger(swaggerUriOrFileContents, auth);
	}

	private void initSwagger(String swaggerUriOrFileContents, Optional<AuthorizationValue> auth) {
		SwaggerDeserializationResult swaggerDeserializationResult = isUrl(swaggerUriOrFileContents) ?
				new SwaggerParser().readWithInfo(swaggerUriOrFileContents, auth.map(Arrays::asList).orElse(null), true) :
				new SwaggerParser().readWithInfo(swaggerUriOrFileContents);
		swagger = swaggerDeserializationResult.getSwagger();
		initSwaggerBasePath();
	}

	private void initSwaggerBasePath() {
		if(swagger.getBasePath() == null) {
			swagger.setBasePath("");
		}
	}

	private void initValidator(String swaggerUriOrFileContents, Optional<AuthorizationValue> auth) {
		Builder builder = SwaggerRequestResponseValidator.createFor(swaggerUriOrFileContents);
		auth.ifPresent(a -> builder.withAuthHeaderData(a.getKeyName(), a.getValue()));
		validator = builder.build();
	}

	/**
	 * Simple function to test, if a string is a valid representation of an URL or not.
	 *
	 * @param content -
	 * @return true is the string is a valid URL
	 */
	private boolean isUrl(String content){
		try {
			new URL(content);
			return true;
		} catch (MalformedURLException e) {
			return false;
		}
	}

	/**
	 * Validiert den Request gegen die YAML.
	 *
	 * @param method -
	 * @param uri -
	 * @param headers -
	 * @param body -
	 */
	public void validateRequest(String method, URI uri, Map<String, List<String>> headers, String body) {
		Method requestMethod = Method.valueOf(method);
		SimpleRequest.Builder requestBuilder = new SimpleRequest.Builder(requestMethod, uri.getPath()).withBody(body);
		if (headers != null) {
			headers.forEach(requestBuilder::withHeader);
		}
		String rawQuery = uri.getQuery();
		Map<String, List<String>> parsedQueryParams = new HashMap<>();
		if (rawQuery != null && !rawQuery.isEmpty()) {
			String[] queryParams = rawQuery.split("&");
			for (String queryParam : queryParams) {
				String[] split = queryParam.split("=");
				if (split.length == 2) {
					String key = split[0];
					String value = split[1];
					if(parsedQueryParams.containsKey(key)) {
						parsedQueryParams.get(key).add(value);
					} else {
						List<String> values = new ArrayList<>();
						values.add(value);
						parsedQueryParams.put(key, values);
					}
				}
			}
		}
		parsedQueryParams.forEach(requestBuilder::withQueryParam);
		SimpleRequest simpleRequest = requestBuilder.build();
		ValidationReport validationReport = validator.validateRequest(simpleRequest);

		ApiOperationMatch apiOperation = getApiOperation(swagger, uri.getPath(), Method.valueOf(method));
		if (apiOperation.isPathFound() && apiOperation.isOperationAllowed()) {
			Collection<Message> validationHeaderMessages = apiOperation.getApiOperation().getOperation().getParameters()
					.stream()
					.filter(param -> "header".equalsIgnoreCase(param.getIn()) && param.getRequired() && (headers == null
							|| !headers.containsKey(param.getName()))).map(param -> new HeaderMessage(param.getName(),
							String.format("Mandatory header \"%s\" is not set.", param.getName())))
					.collect(Collectors.toList());
			ValidationReport validationHeaderReport = ValidationReport.from(validationHeaderMessages);

			ValidationReport mergedValidationReport = validationReport.merge(validationHeaderReport);
			processValidationReport(mergedValidationReport);
		} else {
			LOGGER.info("Request für URI: {} wurde nicht validiert.", uri);
		}
	}

	private void processValidationReport(ValidationReport validationReport) {
		try {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Validierungsergebnis SwaggerUnit: {}", new ObjectMapper().writeValueAsString(validationReport));
			}
		} catch (JsonProcessingException e) {
			LOGGER.error("Das Validierungsergebnis von SwaggerUnit konnte nicht ausgegeben werden.", e);
		}
		if (validationReport != null && validationReport.hasErrors()) {
			String message = validationReport.getMessages().stream()
					//TODO: filter by parameter?
					.map(Message::getMessage).collect(Collectors.joining(" "));
			if (!message.isEmpty()) {
				throw new SwaggerValidationException(message);
			}
		}
	}

	public ApiOperationMatch getApiOperation(Swagger swagger, String path, Request.Method method) {
		return new ApiOperationResolver(swagger, null).findApiOperation(path, method);
	}

	public void validateResponse(String method, int statusCode, URI uri, Map<String, List<String>> headers, String body) {
		SimpleResponse.Builder responseBuilder = new SimpleResponse.Builder(statusCode).withBody(body);
		if (headers != null) {
			headers.forEach((k, v) -> responseBuilder.withHeader(k, v.toArray(new String[0])));
		}

		ApiOperationMatch apiOperation = getApiOperation(swagger, uri.getPath(), Method.valueOf(method));
		// Only validate if path exists in swagger
		if (apiOperation.isPathFound() && apiOperation.isOperationAllowed()) {
			SimpleResponse response = responseBuilder.build();
			ValidationReport validationReport = validator.validateResponse(uri.getPath(), Method.valueOf(method), response);
			processValidationReport(validationReport);
		} else {
			LOGGER.info("Response für URI: {} wurde nicht validiert.", uri);
		}
	}
}
