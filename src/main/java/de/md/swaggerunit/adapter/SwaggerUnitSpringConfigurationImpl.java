// #***************************************************************************
// # mobilcom Vertrieb & Kunde Services - Source File: BaseTestConfiguration.java
// # Copyright (c) 1996-2018 by mobilcom-debitel GmbH
// # Author: mmalitz, Created on: 21.03.2018
// # All rights reserved.
// #***************************************************************************
package de.md.swaggerunit.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.md.swaggerunit.core.SwaggerUnitConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan("de.md.swaggerunit")
public class SwaggerUnitSpringConfigurationImpl implements SwaggerUnitConfiguration {

	@Value("${swaggerSourceOverride}")
	private String swaggerSourceOverride;

	@Value("${swaggerLoginUrl}")
	private String swaggerLoginUrl;

	@Value("${swaggerLoginUsername}")
	private String swaggerLoginUsername;

	@Value("${swaggerLoginPassword}")
	private String swaggerLoginPassword;    //NOSONAR

	@Override
	public String getSwaggerSourceOverride() {
		return swaggerSourceOverride;
	}

	@Override
	public String getSwaggerLoginUrl() {
		return swaggerLoginUrl;
	}

	@Override
	public String getSwaggerLoginUsername() {
		return swaggerLoginUsername;
	}

	@Override
	public String getSwaggerLoginPassword() {
		return swaggerLoginPassword;
	}

	@Bean
	public RestTemplate swaggerUnitHttpClient(@Autowired ObjectMapper objectMapper) {
		RestTemplate swaggerUnitHttpClient = new RestTemplate();
		swaggerUnitHttpClient.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});
		// Suche nach dem MessageConverter für JSON (Jackson) und setze dort den vorkonfigurierten ObjectMapper
		swaggerUnitHttpClient.getMessageConverters().forEach(httpMessageConverter -> {
			if (httpMessageConverter instanceof MappingJackson2HttpMessageConverter) {
				((MappingJackson2HttpMessageConverter) httpMessageConverter).setObjectMapper(objectMapper);
			}
		});
		return swaggerUnitHttpClient;
	}

}
