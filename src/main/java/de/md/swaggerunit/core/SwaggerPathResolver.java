// #***************************************************************************
// # mobilcom Vertrieb & Kunde Services - Source File: SwaggerPathResolver.java
// # Copyright (c) 1996-2018 by mobilcom-debitel GmbH
// # Author: mmalitz, Created on: 13.04.2018
// # All rights reserved.
// #***************************************************************************
package de.md.swaggerunit.core;

import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;

import io.swagger.models.Path;
import io.swagger.models.Swagger;

public class SwaggerPathResolver {

	public SwaggerPathResolver() {
		super();
	}

	public Path resolve(Swagger swagger, URI uri) {
		/** check if the paths exists **/
		String pathToSearchFor = uri.getPath().substring(swagger.getBasePath().length());
		Path path = findExactlyMatchingPath(swagger, pathToSearchFor);
		if (path == null) {
			path = findMatchingParameterPath(swagger, pathToSearchFor);
		}
		return path;
	}

	private Path findExactlyMatchingPath(Swagger swagger, String pathToSearchFor) {
		Map<String, Path> paths = swagger.getPaths();
		return paths.get(pathToSearchFor);
	}

	private Path findMatchingParameterPath(Swagger swagger, String pathToSearchFor) {
		for (String keyToCheck : swagger.getPaths().keySet()) {
			//make regex of path to get something like /v1/contracts/{contractId}
			String pathToCheck = keyToCheck.replaceAll("\\{.*\\}", ".*");
			Pattern pathRegexToCheck = Pattern.compile(pathToCheck);
			boolean equals = pathRegexToCheck.matcher(pathToSearchFor).matches();
			if (equals) {
				return swagger.getPath(keyToCheck);
			}
		}
		return null;
	}

}
