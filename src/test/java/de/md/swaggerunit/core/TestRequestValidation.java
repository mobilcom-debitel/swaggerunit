package de.md.swaggerunit.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class TestRequestValidation {

//	private static final String SWAGGER_DEFINITION = "http://microsrv-d1.mobilcom.de:8016/docs/tariff-swap-service.yaml";

	public static final String SWAGGER_DEFINITION = "{\"swagger\":\"2.0\",\"info\":{\"description\":\"Tarifwechsel-Api\\n\",\"version\":\"1.0.0\",\"title\":\"Tarifwechsel\",\"contact\":{\"name\":\"Team CLM Services\",\"url\":\"http://www.md.de\",\"email\":\"it.ccs.clm.services@md.de\"},\"license\":{\"name\":\"All rights reserved\"}},\"basePath\":\"/v1\",\"host\":\"microsrv-d1:30116\",\"schemes\":[\"http\"],\"consumes\":[\"application/json;charset=utf-8\",\"application/json\"],\"produces\":[\"application/json;charset=utf-8\",\"application/json\"],\"x-vars\":[{\"contractId\":\"MC.212409450\",\"Channel\":\"APP\",\"salesPartnerId\":\"2126\",\"limit\":10,\"offset\":0,\"Agent\":\"Mr. X\"}],\"paths\":{\"/contracts/tariffSwap\":{\"get\":{\"tags\":[\"tariffs\"],\"summary\":\"Tarifwechselangebote.\",\"description\":\"Liefert für die angebene Vertragsnummer die Tarifwechselangebote.\",\"parameters\":[{\"name\":\"contractId\",\"in\":\"query\",\"description\":\"Eindeutige Vertragsnummer\",\"type\":\"string\",\"required\":true},{\"name\":\"limit\",\"in\":\"query\",\"format\":\"int32\",\"description\":\"Maximale Anzahl der gelieferten Einträge,\",\"type\":\"integer\",\"required\":false,\"default\":10},{\"name\":\"offset\",\"in\":\"query\",\"format\":\"int32\",\"description\":\"Anzahl der zu überspringenden Einträge, bzw Startposition in der Ergebnisliste .\",\"type\":\"integer\",\"default\":0,\"required\":false},{\"name\":\"Channel\",\"in\":\"header\",\"type\":\"string\",\"required\":true,\"description\":\"Der Offer-Channel z.B. MYMCIS, PRIVATESERVICE, [...]\"},{\"name\":\"Agent\",\"in\":\"header\",\"description\":\"Der User (Agent, Kunde, etc) welcher das Cart buchen möchte\",\"type\":\"string\",\"required\":true}],\"responses\":{\"200\":{\"description\":\"Ok\",\"schema\":{\"$ref\":\"#/definitions/TariffOrders\"}},\"400\":{\"description\":\"Fehlerhafte Anfrage\",\"schema\":{\"$ref\":\"#/definitions/ErrorResponse\"}},\"403\":{\"description\":\"Der User hat nicht das Recht die Aktion durchzuführen\",\"schema\":{\"$ref\":\"#/definitions/ErrorResponse\"}},\"500\":{\"description\":\"Backend (teilweise) nicht erreichbar.\",\"schema\":{\"$ref\":\"#/definitions/ErrorResponse\"}}}},\"post\":{\"tags\":[\"tariffs\"],\"summary\":\"Bestellt einen Tarifwechsel.\",\"description\":\"Zu der angegebenen Vertragsnummer wird ein Tarifwechsel durchgeführt.\",\"parameters\":[{\"name\":\"body\",\"in\":\"body\",\"schema\":{\"$ref\":\"#/definitions/TariffOrder\"},\"required\":true},{\"name\":\"Channel\",\"in\":\"header\",\"type\":\"string\",\"required\":true,\"description\":\"Der Offer-Channel z.B. MYMCIS, PRIVATESERVICE, [...]\"},{\"name\":\"Agent\",\"in\":\"header\",\"description\":\"Der User (Agent, Kunde, etc) welcher das Cart buchen möchte\",\"type\":\"string\",\"required\":true}],\"responses\":{\"201\":{\"description\":\"Ok\",\"schema\":{\"$ref\":\"#/definitions/TariffOrders\"}},\"400\":{\"description\":\"Fehlerhafte Anfrage\",\"schema\":{\"$ref\":\"#/definitions/ErrorResponse\"}},\"403\":{\"description\":\"Der User hat nicht das Recht die Aktion durchzuführen\",\"schema\":{\"$ref\":\"#/definitions/ErrorResponse\"}},\"500\":{\"description\":\"Backend (teilweise) nicht erreichbar.\",\"schema\":{\"$ref\":\"#/definitions/ErrorResponse\"}}}}}},\"definitions\":{\"TariffOrder\":{\"description\":\"Daten für Tarifwechsel\",\"type\":\"object\",\"required\":[\"tariffId\",\"contractId\"],\"properties\":{\"contractId\":{\"type\":\"string\"},\"tariffId\":{\"type\":\"string\",\"description\":\"Id des Vertrages in dem gewechelt werden soll\",\"example\":\"VA100D1|11409\"},\"street\":{\"type\":\"string\",\"description\":\"Straße ohne Hausnummer, nur bei Homezone anzugeben.\",\"example\":\"Hollerstrasse\"},\"streetNumber\":{\"type\":\"string\",\"description\":\"Hausnummer, nur bei Homezone anzugeben.\",\"example\":\"17a\"},\"postalCode\":{\"type\":\"string\",\"description\":\"Postleitzahl, nur bei Homezone anzugeben.\",\"example\":\"21897\"},\"city\":{\"type\":\"string\",\"description\":\"Stadt, nur bei Homezone anzugeben.\",\"example\":\"Hamburg\"}}},\"TariffOrders\":{\"description\":\"Liste aus Vertragsangeboten\",\"type\":\"object\",\"properties\":{\"tariffs\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/Tariffs\"}}}},\"Tariffs\":{\"description\":\"Tarifwechselangebot\",\"type\":\"object\",\"required\":[\"id\",\"label\",\"priceRecurrent\",\"fee\"],\"properties\":{\"id\":{\"type\":\"string\",\"description\":\"Eindeutige Tarifkennzeichnung.\",\"example\":\"213124\"},\"label\":{\"type\":\"string\",\"description\":\"Tarif-Anzeigetext.\",\"example\":\"RED 4 GB mit Handy 10\"},\"priceRecurrent\":{\"type\":\"string\",\"description\":\"Monatspreis.\",\"example\":\"39.99\",\"format\":\"number\"},\"fee\":{\"type\":\"string\",\"description\":\"Wechselgebühr.\",\"example\":\"19.99\"},\"date\":{\"type\":\"string\",\"format\":\"date-time\",\"description\":\"Ausführungsdatum zu dem eine Tarifwechsel durchgeführt werden kann.\"},\"netCode\":{\"type\":\"string\",\"description\":\"Netz\",\"example\":\"D1\"},\"mustProlongate\":{\"type\":\"boolean\",\"description\":\"Laufzeitverlängerung\"}}},\"ErrorResponse\":{\"type\":\"object\",\"properties\":{\"status\":{\"type\":\"integer\",\"format\":\"int32\",\"description\":\"Der HTTP-Status Code\"},\"code\":{\"type\":\"integer\",\"description\":\"Benutzerdefinierte Fehlercode\"},\"alphaCode\":{\"type\":\"string\",\"description\":\"Benutzerdefinierte alphanumerischer Fehlercode\"},\"moreInfo\":{\"type\":\"string\",\"description\":\"Zusätzliche Informationen zu der Fehlermeldung\"},\"message\":{\"type\":\"string\",\"description\":\"Die eigentliche Fehlermeldung\"},\"developerMessages\":{\"type\":\"array\",\"description\":\"Eine Liste von Nachrichten für die Entwickler. Diese Informationen sollten nicht nach außen getragen werden.\",\"items\":{\"type\":\"string\"}},\"property\":{\"type\":\"string\",\"description\":\"Welche property betroffen ist.\"},\"origin\":{\"type\":\"string\",\"description\":\"Ursprung des Fehlers (Servicename, Servername, ...) \"},\"timestamp\":{\"type\":\"string\",\"format\":\"date-time\",\"description\":\"Der Zeitpunkt an dem der Fehler aufgetreten ist\"},\"requestId\":{\"type\":\"string\",\"description\":\"RequestId für Analysezwecke\"}}}}}";

	@Test
	public void testValidation_shouldPass()  {
		SwaggerUnitCore swaggerUnitCore = new SwaggerUnitCore(SWAGGER_DEFINITION);
		URI toTest = URI.create("/v1/contracts/tariffSwap?contractId=mc.123324");

		Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
			put("Channel", Collections.singletonList("App"));
			put("Agent", Collections.singletonList("Fox Mulder"));
		}};

		swaggerUnitCore.validateRequest("GET", toTest, headers, null);
	}

	@Test
	public void oneHeaderMissing_shouldFail() throws JsonProcessingException {
		try {
			SwaggerUnitCore swaggerUnitCore = new SwaggerUnitCore(SWAGGER_DEFINITION);
			URI toTest = URI.create("/v1/contracts/tariffSwap?contractId=mc.123324");

			Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
				put("Channel", Collections.singletonList("App"));
			}};

			swaggerUnitCore.validateRequest("GET", toTest, headers, null);
			fail("test should have failed!");
		}
		catch(SwaggerValidationException ex){
			assertNotNull(ex);
			assertNotNull(ex.getMessage());
			assertEquals("error message is unexpected!", "Mandatory header \"Agent\" is not set.", ex.getMessage());
		}
	}

	@Test
	public void allHeaderMissing_shouldFail() throws JsonProcessingException {
		try {
			SwaggerUnitCore swaggerUnitCore = new SwaggerUnitCore(SWAGGER_DEFINITION);
			URI toTest = URI.create("/v1/contracts/tariffSwap?contractId=mc.123324");

			Map<String, List<String>> headers = new HashMap<>();

			swaggerUnitCore.validateRequest("GET", toTest, headers, null);
			fail("test should have failed!");
		}
		catch(SwaggerValidationException ex){
			assertNotNull(ex);
			assertNotNull(ex.getMessage());
			assertTrue(ex.getMessage().contains("Mandatory header \"Agent\" is not set."));
			assertTrue(ex.getMessage().contains("Mandatory header \"Channel\" is not set."));
		}
	}

	@Test
	public void queryParamIsMissing_shouldFail(){
		try {
			SwaggerUnitCore swaggerUnitCore = new SwaggerUnitCore(SWAGGER_DEFINITION);
			URI toTest = URI.create("/v1/contracts/tariffSwap");

			Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
				put("Channel", Collections.singletonList("App"));
				put("Agent", Collections.singletonList("Fox Mulder"));
			}};

			swaggerUnitCore.validateRequest("GET", toTest, headers, null);
			fail("test should have failed!");
		}
		catch(SwaggerValidationException ex){
			assertNotNull(ex);
			assertNotNull(ex.getMessage());
			assertTrue(ex.getMessage().contains("Query parameter 'contractId' is required on path '/contracts/tariffSwap' but not found in request."));
		}
	}

	@Test
	public void shouldIgnoreAdditionalHeaders_shouldPass(){
		SwaggerUnitCore swaggerUnitCore = new SwaggerUnitCore(SWAGGER_DEFINITION);
		URI toTest = URI.create("/v1/contracts/tariffSwap?contractId=mc.123324");

		Map<String, List<String>> headers = new HashMap<String, List<String>>() {{
			put("Channel", Collections.singletonList("App"));
			put("Agent", Collections.singletonList("Fox Mulder"));
			put("Something", Collections.singletonList("Somewhere"));
		}};

		swaggerUnitCore.validateRequest("GET", toTest, headers, null);
	}
}
