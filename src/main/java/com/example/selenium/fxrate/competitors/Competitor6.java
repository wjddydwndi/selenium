package com.example.selenium.fxrate.competitors;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.fxrate.AbstractCompetitor;
import com.example.selenium.fxrate.etc.RequestMethodPost;
import com.example.selenium.model.Response;
import com.example.selenium.model.selenium.RequestParam;
import com.example.selenium.model.selenium.Selenium;
import com.example.selenium.service.selenium.SeleniumConfigService;
import com.example.selenium.service.selenium.SeleniumService;
import com.google.api.client.util.Value;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.selenium.common.util.CommonUtils.isEmpty;
import static com.example.selenium.common.util.Commons.*;

@Component
@RequiredArgsConstructor
public class Competitor6 extends AbstractCompetitor {

    @Value("${competitors.competitor6}")
    private String url;

    private final String name = COMPETITOR5.CODE();
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
        List<RequestMethodPost> requestMethodPostList = (List<RequestMethodPost>) createRequestData();

        // 2. Thread call - Data collection
        List<Response> list = threadRun(COMPETITOR6.CODE(), requestMethodPostList);

        // 3. Processing stored data
        return createStoredData(list, cronAt, COMPETITOR6.CODE());
    }

    @Override
    public List<?> createRequestData() {

        List<RequestParam> list = seleniumConfigService.getParameters(COMPETITOR6.CODE());
        Map<String, String> headers = getHeaders(chromeDriver, url);

        List<RequestMethodPost> requestDataList = new ArrayList<>();
        String requestUrl = "";// insert into requestUrl

        for (RequestParam item : list) {

            String country = item.getCountry();
            String currency = item.getCurrency();
            String sendAmount = item.getSendAmount();
            String paymentType = item.getPaymentType();
            String countryCode = item.getCountryEnCode();
            String centerCode = item.getCenterCode();

            JSONObject jsonData = new JSONObject();
            jsonData.put("inputAmount", sendAmount);
            jsonData.put("inputCurrencyCode", "KRW");
            jsonData.put("lang", "ko");
            jsonData.put("mtoProviderCode", "");
            jsonData.put("mtoServiceCenterCode", centerCode);
            jsonData.put("remittanceOption", paymentType);
            jsonData.put("toCountryCode", countryCode);
            jsonData.put("toCurrencyCode", currency);

            try {
                requestDataList.add(new RequestMethodPost(COMPETITOR6.CODE(), country, requestUrl, headers, jsonData.toJSONString()));

            } catch (Exception e) {
                Logger.error("[{}] {}, 수집 실패 exception e={}", COMPETITOR6.CODE(), country, e.getMessage());
                continue;
            }
        }

        return requestDataList;
    }


    @Override
    public Response createStoredData(List<?> list, String cronAt, String name) {

        if (isEmpty(list) || isEmpty(cronAt) || isEmpty(name)) {
            return new Response(RESPONSE_FAIL.CODE(), "fail", null, name);
        }

        List<Selenium> response = new ArrayList<>();

        List<Response> responseList = (List<Response>) list;

        for (Response item : responseList) {

            String country = isEmpty(item.getCountry()) ? "" : item.getCountry();

            if (!item.getCode().equals(RESPONSE_SUCCESS.CODE())) {
                Logger.info("{}, 수집 실패 country={}", country);
                continue;
            }

            Selenium selenium = new Selenium();

            try {
                org.json.JSONObject jsonObject = (org.json.JSONObject) item.getData();

                String toCurrency = String.valueOf(jsonObject.get("toCurrencyCode"));
                String toAmount = String.valueOf(jsonObject.get("toAmount"));
                float recvAmount = Float.parseFloat(toAmount);

                selenium.setCronAt(cronAt);
                selenium.setCompany(COMPETITOR6.CODE().toLowerCase());
                selenium.setCountry(country);
                selenium.setCurrency(toCurrency);
                selenium.setPrice(String.format("%.8f", recvAmount));

            } catch (Exception e) {
                Logger.error("[{}] {} 수집 실패, exception e={}, ", COMPETITOR6.CODE(), country, e.getMessage());
                continue;
            }

            response.add(selenium);
        }

        return new Response(RESPONSE_SUCCESS.CODE(), "success", response, name);
    }


    @Override
    public Map<String, String> getHeaders(ChromeDriver chromeDriver, String url) {

        Object obj = chromeDriver.executeScript("return navigator.userAgent");
        String userAgent = String.valueOf(obj);

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*/*");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "ko,en-US;q=0.9,en;q=0.8");
        headers.put("Connection", "keep-alive");
        headers.put("Content-Length", "154");

        headers.put("Content-Type", "application/json");
        headers.put("Host", "");// insert into Host
        headers.put("Origin", "");// insert into Origin
        headers.put("Referer", "");// insert into Referer

        headers.put("sec-ch-ua", "'Google Chrome';v='89', 'Chromium';v='89', ';Not A Brand';v='99'");
        headers.put("sec-ch-ua-mobile", "?0");
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("User-Agent", userAgent);
        headers.put("X-Requested-With", "XMLHttpRequest");

        return headers;
    }


    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}
}
