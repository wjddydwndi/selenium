package com.example.selenium.fxrate.competitors;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.fxrate.AbstractCompetitor;
import com.example.selenium.fxrate.etc.RequestMethodGet;
import com.example.selenium.model.Response;
import com.example.selenium.model.selenium.RequestParam;
import com.example.selenium.model.selenium.Selenium;
import com.example.selenium.service.selenium.SeleniumConfigService;
import com.example.selenium.service.selenium.SeleniumService;
import com.google.api.client.util.Value;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.example.selenium.common.util.CommonUtils.isEmpty;
import static com.example.selenium.common.util.Commons.*;


@Component
@RequiredArgsConstructor
public class Competitor10 extends AbstractCompetitor {

    @Value("${competitors.competitor10}")
    private String url;
    private final String name = COMPETITOR10.CODE();
    private ChromeDriver chromeDriver;
    private final SeleniumService seleniumService;
    private final SeleniumConfigService seleniumConfigService;

    @Override
    public Response call() {return super.call(name, url);}

    @Override
    public Response request() throws IOException, ParseException, NullPointerException {

        LocalDateTime now = super.getCronAt();
        String cronAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 1. create Request Data
        List<Response> responseList = (List<Response>) createRequestData();

        // 2. Processing stored data
        return createStoredData(responseList, cronAt, name);
    }

    @Override
    public List<?> createRequestData() {

        List<Response> responseList = new ArrayList<>();

        try {
            String requestUrl = "";// requestUrl

            RequestMethodGet requestMethodGet = new RequestMethodGet(name, requestUrl);
            Response response = requestMethodGet.request(chromeDriver);

            if (response.getCode().equals(RESPONSE_SUCCESS.CODE())) {
                responseList.add(response);
            }

        } catch (Exception e) {
            Logger.error("[{}]  수집 실패 exception e={}", name, e.getMessage());
        }

        return responseList;
    }

    @Override
    public Response createStoredData(List<?> list, String cronAt, String name) {

        if (isEmpty(list) || isEmpty(cronAt) || isEmpty(name)) {
            return new Response(RESPONSE_FAIL.CODE(), "fail", null, name);
        }

        List<Selenium> response = new ArrayList<>();

        List<RequestParam> parameters = seleniumConfigService.getParameters(name);

        List<Response> responseList = (List<Response>) list;
        String data = String.valueOf(responseList.get(0).getData());
        String commision = data.split("var COMMISSION = ")[1].split(";")[0];
        String sFxrate = data.split("var FXRATE = ")[1].split(";")[0];
        float fxrate = Float.parseFloat(sFxrate);

        for (RequestParam item : parameters) {

            String country = item.getCountry();
            String sendAmount = item.getSendAmount();
            String currency = item.getCurrency();

            Selenium selenium = new Selenium();

            try {
                float fSendAmount = Float.parseFloat(sendAmount);

                float price = Math.round(fSendAmount * fxrate) / 100;

                selenium.setCronAt(cronAt);
                selenium.setCompany(name.toLowerCase());
                selenium.setCountry(country);
                selenium.setCurrency(currency);
                selenium.setPrice(String.format("%.8f", price));

            } catch (Exception e) {
                Logger.error("[{}] {} 수집 실패, exception e={}, ", name, country, e.getMessage());
                continue;
            }

            response.add(selenium);
        }

        return new Response(RESPONSE_SUCCESS.CODE(), "success", response, name);
    }


    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}

}
