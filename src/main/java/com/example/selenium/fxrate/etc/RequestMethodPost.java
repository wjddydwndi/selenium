package com.example.selenium.fxrate.etc;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.fxrate.AbstractCompetitor;
import com.example.selenium.model.Response;
import lombok.NonNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static com.example.selenium.common.util.CommonUtils.isEmpty;
import static com.example.selenium.common.util.CommonUtils.isJsonValid;
import static com.example.selenium.common.util.Commons.RESPONSE_FAIL;
import static com.example.selenium.common.util.Commons.RESPONSE_SUCCESS;


public class RequestMethodPost extends AbstractCompetitor {

    private String name;
    private String country;
    private String url;
    private Map<String, String> headers;
    private String urlEncode;

    public RequestMethodPost(String name, String country, String url, @NonNull Map<String, String> headers, String urlEncode) {
        super();
        this.name = name;
        this.country = country;
        this.url = url;
        this.headers = headers;
        this.urlEncode = urlEncode;
    }

    public RequestMethodPost(String name, String url, @NonNull Map<String, String> headers, String urlEncode) {
        super();
        this.name = name;
        this.url = url;
        this.headers = headers;
        this.urlEncode = urlEncode;
    }

    @Override
    public Response call() {

        Response response = null;
        try {
            response = request();

        } catch (Exception e) {
            Logger.error("{}, exception e={}", name, e.getMessage());
            return new Response(RESPONSE_FAIL.CODE(), "fail", name, country);
        }

        return new Response(RESPONSE_SUCCESS.CODE(), "success", response.getData(), name, country);
    }

    @Override
    public Response request() throws IOException, ParseException, NullPointerException {

        Response response = null;
        String result = sendPost(new URL(url), headers, urlEncode);

        if (isEmpty(result)) {
            new Response(RESPONSE_FAIL.CODE(), "fail", result, name, country);
        }

        if (isJsonValid(result) == true) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(result);
            response = new Response(RESPONSE_SUCCESS.CODE(), "success", jsonObject, name, country);
        } else {
            response = new Response(RESPONSE_SUCCESS.CODE(), "success", result, name, country);
        }

        return response;
    }

    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}

}
