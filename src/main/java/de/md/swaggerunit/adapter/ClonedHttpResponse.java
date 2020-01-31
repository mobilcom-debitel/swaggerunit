package de.md.swaggerunit.adapter;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by fpriede on 26.04.2017.
 */
public class ClonedHttpResponse implements ClientHttpResponse {

    private HttpStatus statusCode;

    private int rawStatusCode;

    private String statusText;

    private HttpHeaders headers;

    private byte[] body;

    private ClonedHttpResponse(){

    }

    public static ClonedHttpResponse createFrom(ClientHttpResponse origin) throws IOException {
        ClonedHttpResponse response = new ClonedHttpResponse();
        response.rawStatusCode = origin.getRawStatusCode();
        response.statusCode = origin.getStatusCode();
        response.headers = origin.getHeaders();
        response.statusText = origin.getStatusText();
        if(origin.getBody() != null){
            response.body = IOUtils.toByteArray(origin.getBody());
        }
        return response;
    }

    byte[] getRawBody(){
        return body;
    }

    @Override
    public HttpStatus getStatusCode() throws IOException {
        return statusCode;
    }

    @Override
    public int getRawStatusCode() throws IOException {
        return rawStatusCode;
    }

    @Override
    public String getStatusText() throws IOException {
        return statusText;
    }

    @Override
    public void close() {

    }

    @Override
    public InputStream getBody() throws IOException {
        return new ByteArrayInputStream(body);
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }
}
