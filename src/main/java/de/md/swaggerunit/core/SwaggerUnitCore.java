package de.md.swaggerunit.core;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.atlassian.oai.validator.SwaggerRequestResponseValidator;
import com.atlassian.oai.validator.SwaggerRequestResponseValidator.Builder;
import com.atlassian.oai.validator.model.Request.Method;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.report.ValidationReport.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.auth.AuthorizationValue;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;

@Component
public class SwaggerUnitCore {

	private static final Logger LOGGER = LoggerFactory.getLogger(SwaggerUnitCore.class);

	@Inject
	private SwaggerAuthentication authentication;
	
	@Inject
	private SwaggerUnitConfiguration swaggerUnitConfiguration;
	
	private SwaggerRequestResponseValidator validator;

	private Swagger swagger;

	public SwaggerUnitCore() {}

	public SwaggerUnitCore(String swaggerDefinition) {
		init(swaggerDefinition, Optional.empty());
	}

	@PostConstruct
	private void init() {
		init(swaggerUnitConfiguration.getSwaggerSourceOverride(), authentication.getAuth());
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
	}

	private void initValidator(String swaggerUriOrFileContents, Optional<AuthorizationValue> auth) {
		Builder builder = SwaggerRequestResponseValidator.createFor(swaggerUriOrFileContents);
		auth.ifPresent(a -> builder.withAuthHeaderData(a.getKeyName(), a.getValue()));
		validator = builder.build();
	}

	/**
	 * Simple function to test, if a string is a valid representation of an URL or not.
	 * @param content
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
	 * @param method
	 * @param uri
	 * @param header
	 * @param body
	 * @throws JsonProcessingException 
	 */
	public void validateRequest(String method, URI uri, Map<String, List<String>> header, String body)  {
		String basePath = swagger.getBasePath();
		if(basePath == null) {
			basePath = "";
		}
		Method requestMethod = Method.valueOf(method);
		String relUri = uri.getPath().substring(basePath.length(), uri.getPath().length());
		SimpleRequest.Builder requestBuilder = new SimpleRequest.Builder(requestMethod, relUri).withBody(body);
		if (header != null) {
			header.forEach(requestBuilder::withHeader);
		}
		String rawQuery = uri.getQuery();
		Map<String, List<String>> parsedQueryParams = new HashMap<>();
		if(rawQuery != null && !rawQuery.isEmpty()) {
			String[] queryParams = rawQuery.split("&");
			for(String queryParam : queryParams) {
				String[] split = queryParam.split("=");
				if(split.length == 2) {
					String key = split[0];
					String value = split[1];
					if(parsedQueryParams.containsKey(key)) {
						parsedQueryParams.get(key).add(value);
					} else {
						List<String> values = new ArrayList<>();
						values.add(value);
						parsedQueryParams.put(key, values);
					}
				} else {
					// log warn/info
				}
			}
		}
		parsedQueryParams.forEach(requestBuilder::withQueryParam);
		SimpleRequest simpleRequest = requestBuilder.build();
    	ValidationReport validationReport = validator.validateRequest(simpleRequest);

		/** check if the paths exists **/
    	String pathToSearchFor = uri.getPath().substring(basePath.length());
    	Path path = null;
    	Map<String, Path> paths = swagger.getPaths();
    	for(String keyToCheck : paths.keySet()) {
    		//make regex of path  to get something like /v1/contracts/{contractId}
    		String pathToCheck = keyToCheck.replaceAll("\\{.*\\}", ".*");
    		Pattern pathRegexToCheck = Pattern.compile(pathToCheck);
    		boolean equals = pathRegexToCheck.matcher(pathToSearchFor).matches();
    		if(equals) {
    			path = swagger.getPath(keyToCheck);
    			break;
    		}
    	}
		if (path == null) {
			throw new SwaggerValidationException(String.format("unable to find path for \"%s\".", pathToSearchFor));
		}


		Collection<Message> validationHeaderMessages = getOperationForMethodFromPath(path, requestMethod).getParameters()
				.stream()
				.filter(param -> "header".equalsIgnoreCase(param.getIn()) && param.getRequired()
						&& (header == null || !header.containsKey(param.getName())))
				.map(param -> new HeaderMessage(param.getName(),
						String.format("Mandatory header \"%s\" is not set.", param.getName())))
				.collect(Collectors.toList());
		ValidationReport validationHeaderReport = ValidationReport.from(validationHeaderMessages);
		
		ValidationReport mergedValidationReport =  validationReport.merge(validationHeaderReport);
		processValidationReport(mergedValidationReport);
	}

	private void processValidationReport(ValidationReport validationReport) {
		try {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Validierungsergebnis SwaggerUnit: {}", new ObjectMapper().writeValueAsString(validationReport));
			}
		} catch (JsonProcessingException e) {
			LOGGER.error("Das Validierungsergebnis von SwaggerUnit konnte nicht ausgegeben werden.", e);
		}
		if(validationReport != null && validationReport.hasErrors()){
			String message = validationReport.getMessages().stream()
					//TODO: filter by parameter?
					.map(Message::getMessage)
					.collect(Collectors.joining(" "));
			if(message != null && !message.isEmpty()) {
				throw new SwaggerValidationException(message);
			}
		}
	}

	private Operation getOperationForMethodFromPath(Path path, Method method){
		switch(method){
			case DELETE:
				return path.getDelete();
			case GET:
				return path.getGet();
			case PATCH:
				return path.getPatch();
			case POST:
				return path.getPost();
			case PUT:
				return path.getPut();
			default:
				//just in case one day TRACE or so will be added to the Method enum ^^.
				throw new UnsupportedOperationException(String.format("http verb \"%s\" is not supported.", method));
		}
	}

	public void validateResponse(String method, int statusCode, URI uri, Map<String, List<String>> headers, String body) {
		SimpleResponse.Builder responseBuilder = new SimpleResponse.Builder(statusCode).withBody(body);
		if(headers != null){
			headers.forEach((k, v) -> {
				responseBuilder.withHeader(k, v.toArray(new String[v.size()]));
			});
		}
		String basePath = swagger.getBasePath();
		if(basePath == null) {
			basePath = "";
		}
		String relUri = uri.getPath().substring(basePath.length());
		SimpleResponse response = responseBuilder.build();
		ValidationReport validationReport = validator.validateResponse(relUri, Method.valueOf(method), response);
		processValidationReport(validationReport);
	}
}
