package com.example.selenium.fxrate.etc;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.fxrate.AbstractCompetitor;
import com.example.selenium.model.Response;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static com.example.selenium.common.util.CommonUtils.isJsonValid;
import static com.example.selenium.common.util.Commons.RESPONSE_FAIL;
import static com.example.selenium.common.util.Commons.RESPONSE_SUCCESS;

public class RequestMethodGet extends AbstractCompetitor {

    private String name;
    private String country;
    private Map<String, String> headers;
    private String param;
    private String url;

    public RequestMethodGet(String name, String url) {
        super();
        this.name = name;
        this.url = url;
    }

    public RequestMethodGet(String name, String country, String url) {
        super();
        this.name = name;
        this.country = country;
        this.url = url;
    }

    public RequestMethodGet(String name, String country, String url, Map<String, String> headers, String param) {
        super();
        this.name = name;
        this.country = country;
        this.url = url;
        this.headers = headers;
        this.param = param;
    }

    public RequestMethodGet(String name, String url, Map<String, String> headers) {
        super();
        this.name = name;
        this.country = country;
        this.url = url;
        this.headers = headers;
        this.param = param;
    }

    public RequestMethodGet(String name, String url, Map<String, String> headers, String param) {
        super();
        this.name = name;
        this.country = country;
        this.url = url;
        this.headers = headers;
        this.param = param;
    }

    @Override
    public Response call() {

        Response response = null;

        try {
            response = request();

        } catch (Exception e) {
            Logger.error("{}, exception e={}", name, e.getMessage());
            return new Response(RESPONSE_FAIL.CODE(), "fail", response.getData(),  name, country);
        }

        return new Response(RESPONSE_SUCCESS.CODE(), "success", response.getData(), name, country);
    }

    @Override
    public Response request() throws IOException, ParseException, NullPointerException {

        Response response = null;

        String result = sendGet(new URL(url));

        if (isJsonValid(result) == true) {
            response = new Response(RESPONSE_SUCCESS.CODE(), "success", new JSONObject(result), name);
        } else {
            response = new Response(RESPONSE_SUCCESS.CODE(), "success", result, name);
        }

        return response;
    }

    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException{return null;}

}
