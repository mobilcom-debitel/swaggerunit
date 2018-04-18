// #***************************************************************************
// # mobilcom Vertrieb & Kunde Services - Source File: BaseTestConfiguration.java
// # Copyright (c) 1996-2018 by mobilcom-debitel GmbH
// # Author: mmalitz, Created on: 21.03.2018
// # All rights reserved.
// #***************************************************************************
package de.md.swaggerunit.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan("de.md.swaggerunit")
public class SwaggerUnitConfiguration {

	@Value("${swaggerSourceOverride}")
	private String swaggerSourceOverride;

	@Value("${swaggerLoginUrl}")
	private String swaggerLoginUrl;
	
	@Value("${swaggerLoginUsername}")
	private String swaggerLoginUsername;
	
	@Value("${swaggerLoginPassword}")
	private String swaggerLoginPassword;	//NOSONAR
	
	public String getSwaggerSourceOverride() {
		return swaggerSourceOverride;
	}
	
	public String getSwaggerLoginUrl() {
		return swaggerLoginUrl;
	}
	
	public String getSwaggerLoginUsername() {
		return swaggerLoginUsername;
	}
	
	public String getSwaggerLoginPassword() {
		return swaggerLoginPassword;
	}

	@Bean
	public RestTemplate swaggerUnitHttpClient() {
		RestTemplate swaggerUnitHttpClient = new RestTemplate();
		swaggerUnitHttpClient.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});
		return swaggerUnitHttpClient;
	}

	@Bean
	public SwaggerPathResolver swaggerPathResolver() {
		return new SwaggerPathResolver();
	}

}
