package com.example.selenium.fxrate.competitors;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.fxrate.AbstractCompetitor;
import com.example.selenium.fxrate.etc.RequestMethodGet;
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
import java.util.*;

import static com.example.selenium.common.util.CommonUtils.isEmpty;
import static com.example.selenium.common.util.Commons.*;

@Component
@RequiredArgsConstructor
public class Competitor7 extends AbstractCompetitor {
    @Value("${competitors.competitor7}")
    private String url;
    private ChromeDriver chromeDriver;

    private final String name = COMPETITOR7.CODE();
    private final SeleniumService seleniumService;
    private final SeleniumConfigService seleniumConfigService;

    @Override
    public Response call() {return super.call(name, url);}

    @Override
    public Response request() throws IOException, ParseException, NullPointerException {

        LocalDateTime now = super.getCronAt();
        String cronAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 1. create Request Data
        List<RequestMethodGet> list = (List<RequestMethodGet>) createRequestData();

        // 2. Thread call - Data collection
        List<Response> responseList = threadRun(name, list);

        // 3. Processing stored data
        return createStoredData(responseList, cronAt, name);
    }


    @Override
    public List<?> createRequestData() {

        List<RequestParam> list = seleniumConfigService.getParameters(name);

        Map<String, String> headers = getHeaders(chromeDriver, url);
        List<RequestMethodPost> requestDataList = new ArrayList<>();
        String requestUrl = "";// insert into requestUrl

        for (RequestParam item : list) {

            String country = item.getCountry();
            String currency = item.getCurrency();
            String sendAmount = item.getSendAmount();

            JSONObject jsonData = new JSONObject();
            jsonData.put("country", currency);
            jsonData.put("sendmoney", sendAmount);
            jsonData.put("type", "Bank Transfer");
            jsonData.put("id", "country");
            jsonData.put("receiveMoney", 0);

            try {
                String strJsonData = jsonData.toJSONString();
                headers.put("Content-Length", String.valueOf(strJsonData.length()));

                requestDataList.add(new RequestMethodPost(name, country, requestUrl, headers, jsonData.toJSONString()));

            } catch (Exception e) {
                Logger.error("[{}] {}, 수집 실패 exception e={}", name, country, e.getMessage());
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

        List<RequestParam> parameters = seleniumConfigService.getParameters(name);

        List<Response> responseList = (List<Response>) list;

        for (Response item : responseList) {

            String country = isEmpty(item.getCountry()) ? "" : item.getCountry();

            if (!item.getCode().equals(RESPONSE_SUCCESS.CODE())) {
                Logger.info("{}, 수집 실패 country={}", country);
                continue;
            }

            Selenium selenium = new Selenium();

            try {

                Optional<RequestParam> optional = parameters.stream().filter(x-> x.getCountry().equalsIgnoreCase(item.getCountry())).findAny();

                if (!optional.isPresent()) continue;

                org.json.JSONObject jsonObject = (org.json.JSONObject) item.getData();
                org.json.JSONObject object = (org.json.JSONObject) jsonObject.get("d");

                String sendAmount = optional.get().getSendAmount();
                String currency = optional.get().getCurrency();
                String serviceFee = String.valueOf(object.get("ServiceFee"));
                String customerRate = String.valueOf(object.get("customer_rate"));

                float fSendAmount = Float.parseFloat(sendAmount);
                float fServiceFee = Float.parseFloat(serviceFee);
                float fCustomerRate = Float.parseFloat(customerRate);

                float price = Math.round((fSendAmount - fServiceFee) * fCustomerRate * 100) / 100;

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
    public Map<String, String> getHeaders(ChromeDriver chromeDriver, String url) {

        Object obj = chromeDriver.executeScript("return navigator.userAgent");
        String userAgent = String.valueOf(obj);

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "ko,en-US;q=0.9,en;q=0.8");
        //headers.put("Content-Length", "keep-alive");
        headers.put("Content-Type", "application/json");
        headers.put("Host", "");// host
        headers.put("Origin", "");// origin
        headers.put("Referer", "");// referer
        headers.put("sec-ch-ua", "'Chromium';v='94', 'Google Chrome';v='94', ';Not A Brand';v='99'");
        headers.put("sec-ch-ua-mobile", "?0");

        headers.put("sec-fetch-dest", "empty");
        headers.put("sec-fetch-mode", "empty");
        headers.put("sec-fetch-site", "same-origin");
        headers.put("user-agent", userAgent);
        headers.put("x-requested-with", "XMLHttpRequest");

        return headers;
    }


    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}
}
