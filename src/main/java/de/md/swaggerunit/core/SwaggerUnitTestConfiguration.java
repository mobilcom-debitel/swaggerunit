// #***************************************************************************
// # mobilcom Vertrieb & Kunde Services - Source File: BaseTestConfiguration.java
// # Copyright (c) 1996-2018 by mobilcom-debitel GmbH
// # Author: mmalitz, Created on: 21.03.2018
// # All rights reserved.
// #***************************************************************************
package de.md.swaggerunit.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
@ComponentScan("de.md.swaggerunit")
public class SwaggerUnitTestConfiguration {

	@Value("${swaggerSourceOverride}")
	private String swaggerSourceOverride;

	@Value("${swaggerLoginUrl:https://developers.md.de/api/login}")
	private String swaggerLoginUrl;
	
	@Value("${swaggerLoginUsername:it.ccs.clm.services@md.de}")
	private String swaggerLoginUsername;
	
	@Value("${swaggerLoginPassword:xavinulajo12}")
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
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});
		return restTemplate;
	}

}
