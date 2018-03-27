// #***************************************************************************
// # mobilcom Vertrieb & Kunde Services - Source File: SwaggerAuthenticationTest.java
// # Copyright (c) 1996-2018 by mobilcom-debitel GmbH
// # Author: mmalitz, Created on: 22.03.2018
// # All rights reserved.
// #***************************************************************************
package de.md.swaggerunit.core;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.swagger.models.auth.AuthorizationValue;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class SwaggerAuthenticationTest {

	@Injectable
	SwaggerUnitConfiguration swaggerUnitConfiguration;

	@Test
	public void testGetAuthReturnsValidAuthorizationWithToken(@Tested SwaggerAuthentication swaggerAuthentication,
			@Injectable RestTemplate restTemplate, @Mocked SwaggerAuthenticationResponse swaggerAuthenticationResponse) {
		final String expectedToken = "this_is_my_token";
		new Expectations() {
			{
				swaggerAuthenticationResponse.getToken();
				result = expectedToken;
				restTemplate.postForObject(anyString, any, SwaggerAuthenticationResponse.class);
				result = swaggerAuthenticationResponse;
			}
		};
		Optional<AuthorizationValue> auth = swaggerAuthentication.getAuth();
		assertThat(auth.isPresent(), is(equalTo(true)));
		assertThat(auth.get().getValue(), is(equalTo("Bearer " + expectedToken)));
		assertThat(auth.get().getKeyName(), is(equalTo("Authorization")));
		assertThat(auth.get().getType(), is(equalTo("header")));
	}

	@Test
	public void testGetAuthReturnsEmptyAuthorization(@Tested SwaggerAuthentication swaggerAuthentication,
			@Injectable RestTemplate restTemplate, @Mocked SwaggerAuthenticationResponse swaggerAuthenticationResponse) {
		new Expectations() {
			{
				swaggerAuthenticationResponse.getToken();
				result = null;
				restTemplate.postForObject(anyString, any, SwaggerAuthenticationResponse.class);
				result = swaggerAuthenticationResponse;
			}
		};
		Optional<AuthorizationValue> auth = swaggerAuthentication.getAuth();
		assertThat(auth.isPresent(), is(equalTo(false)));
	}

	@Test
	public void testThatAuthRequestIsPerformedOnlyOnce(@Tested SwaggerAuthentication swaggerAuthentication,
			@Injectable RestTemplate restTemplate, @Mocked SwaggerAuthenticationResponse swaggerAuthenticationResponse) {
		new Expectations() {
			{
				swaggerAuthenticationResponse.getToken();
				result = "ich bin ein Token";
				restTemplate.postForObject(anyString, any, SwaggerAuthenticationResponse.class);
				times = 1;
				result = swaggerAuthenticationResponse;
			}
		};
		Optional<AuthorizationValue> auth = swaggerAuthentication.getAuth();
		assertThat(auth.isPresent(), is(equalTo(true)));
		auth = swaggerAuthentication.getAuth();
		assertThat(auth.isPresent(), is(equalTo(true)));
	}

	@Test
	public void testThatAuthRequestGetsRetriedIfAuthenticationHasntBeenSuccessful(
			@Tested SwaggerAuthentication swaggerAuthentication, @Injectable RestTemplate restTemplate,
			@Mocked SwaggerAuthenticationResponse swaggerAuthenticationResponse) {
		new Expectations() {
			{
				swaggerAuthenticationResponse.getToken();
				times = 2;
				result = null;
				restTemplate.postForObject(anyString, any, SwaggerAuthenticationResponse.class);
				result = swaggerAuthenticationResponse;
			}
		};
		Optional<AuthorizationValue> auth = swaggerAuthentication.getAuth();
		assertThat(auth.isPresent(), is(equalTo(false)));
		auth = swaggerAuthentication.getAuth();
		assertThat(auth.isPresent(), is(equalTo(false)));
	}
	
	@Test(expected = RestClientException.class)
	public void testGetAuthRethrowsRestClientException(@Tested SwaggerAuthentication swaggerAuthentication,
			@Injectable RestTemplate restTemplate) {
		new Expectations() {
			{
				restTemplate.postForObject(anyString, any, SwaggerAuthenticationResponse.class);
				result = new RestClientException("Ich werde geworfen, wenn es bei der Authentifizierung Probleme gibt.");
			}
		};
		swaggerAuthentication.getAuth();
	}

}
