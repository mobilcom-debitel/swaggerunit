// #***************************************************************************
// # mobilcom Vertrieb & Kunde Services - Source File: SwaggerPathResolverTest.java
// # Copyright (c) 1996-2018 by mobilcom-debitel GmbH
// # Author: mmalitz, Created on: 17.04.2018
// # All rights reserved.
// #***************************************************************************
package de.md.swaggerunit.core;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import io.swagger.models.Path;
import io.swagger.models.Swagger;
import mockit.Tested;

public class SwaggerPathResolverTest {

	private static final String PATH_WITH_PARAMETER_PLACEHOLDER = "/v1/contracts/optionsSwap/{cartId}";

	private static final String PATH_WITH_PARAMETER = "/v1/contracts/optionsSwap/0815";

	private static final String PATH_ACTUAL_PATH = "/v1/contracts/optionsSwap/additionalLinks";

	@Tested(availableDuringSetup = true)
	private Swagger swagger;

	@Tested
	private SwaggerPathResolver swaggerPathResolver;

	@Before
	public void initSwagger() {
		swagger.setBasePath(EMPTY);
	}
	
	@SuppressWarnings("serial")
	@Test
	public void testThatActualPathGetsResolved() {
		Path expectedPath = new Path();
		Path unexpectedPath = new Path();
		swagger.setPaths(new HashMap<String, Path>() {
			{
				put(PATH_ACTUAL_PATH, expectedPath);
				put(PATH_WITH_PARAMETER_PLACEHOLDER, unexpectedPath);
			}
		});
		URI uri = URI.create(PATH_ACTUAL_PATH);
		Path resolvedPath = swaggerPathResolver.resolve(swagger, uri);
		assertThat(resolvedPath, is(sameInstance(expectedPath)));
	}

	@SuppressWarnings("serial")
	@Test
	public void testThatPathWithParametersGetsResolved() {
		Path expectedPath = new Path();
		Path unexpectedPath = new Path();
		swagger.setPaths(new HashMap<String, Path>() {
			{
				put(PATH_ACTUAL_PATH, unexpectedPath);
				put(PATH_WITH_PARAMETER_PLACEHOLDER, expectedPath);
			}
		});
		URI uri = URI.create(PATH_WITH_PARAMETER);
		Path resolvedPath = swaggerPathResolver.resolve(swagger, uri);
		assertThat(resolvedPath, is(sameInstance(expectedPath)));
	}

	@SuppressWarnings("serial")
	@Test(expected = SwaggerValidationException.class)
	public void testThatNotMatchingParameterThrowsException() {
		swagger.setPaths(new HashMap<String, Path>() {
			{
				put(PATH_ACTUAL_PATH, new Path());
			}
		});
		URI uri = URI.create(PATH_WITH_PARAMETER);
		swaggerPathResolver.resolve(swagger, uri);
	}

	@Test(expected = SwaggerValidationException.class)
	public void testThatExceptionIsBeingThrownInCaseWeHaveNoPathes() {
		swagger.setPaths(new HashMap<String, Path>());
		URI uri = URI.create(PATH_ACTUAL_PATH);
		swaggerPathResolver.resolve(swagger, uri);
	}

}
