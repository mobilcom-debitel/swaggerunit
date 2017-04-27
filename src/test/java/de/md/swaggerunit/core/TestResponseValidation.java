package de.md.swaggerunit.core;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Created by fpriede on 26.04.2017.
 */
public class TestResponseValidation {

    private SwaggerUnitCore core = new SwaggerUnitCore(TestRequestValidation.SWAGGER_DEFINITION);

    @Test
    public void test() throws URISyntaxException {

        try {
            String responseBody = "{\n" +
                    "  \"tariffs\": [\n" +
                    "    {\n" +
                    "      \"idd\": \"213124\",\n" +
                    "      \"label\": \"RED 4 GB mit Handy 10\",\n" +
                    "      \"priceRecurrent\": \"39.99\",\n" +
                    "      \"fee\": \"19.99\",\n" +
                    "      \"date\": \"2017-04-26T08:33:22.782Z\",\n" +
                    "      \"netCode\": \"D1\",\n" +
                    "      \"mustProlongate\": true\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            Map<String, List<String>> headers = new HashMap<>();

            core.validateResponse("GET", 200, URI.create("/contracts/tariffSwap"), headers, responseBody);
        }
        catch(SwaggerValidationException ex){
            assertNotNull("Es wurde keine Exception geworfen!", ex);
//            assertEquals(ex.getMessage());
        }
    }
}
