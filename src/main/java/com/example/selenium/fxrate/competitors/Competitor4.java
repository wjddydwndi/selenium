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
import org.json.JSONObject;
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
public class Competitor4 extends AbstractCompetitor {
    @Value("${competitors.competitor4}")
    private String url;

    private final String name = COMPETITOR4.CODE();
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
        List<RequestMethodPost> list = (List<RequestMethodPost>) createRequestData();

        // 2. Thread call - Data collection
        List<Response> responseList = threadRun(name, list);

        // 3. Processing stored data
        return createStoredData(responseList, cronAt, name);
    }

    public List<?> createRequestData() {

        List<RequestParam> list = seleniumConfigService.getParameters(name);

        Map<String, String> headers = getHeaders(chromeDriver, url);
        List<RequestMethodPost> requestMethodPostList = new ArrayList<>();
        String requestUrl = url;

        for (RequestParam requestParam : list) {

            String country = requestParam.getCountry();
            String currency = requestParam.getCurrency();
            String sendAmount = requestParam.getSendAmount();
            String deliveryMethod = requestParam.getDeliveryMethod();

            Map<String, String> data = new HashMap<>();
            data.put("method", "GetExRate");
            data.put("pCurr", currency);
            data.put("pCountryName", country);
            data.put("collCurr", "KRW");
            data.put("deliveryMethod", deliveryMethod);
            data.put("cAmt", sendAmount);
            data.put("pAmt", "");
            data.put("cardOnline", "false");
            data.put("calBy", "C");

            try {

                String urlEncode = urlEncode(data);
                requestMethodPostList.add(new RequestMethodPost(name, country, requestUrl, headers, urlEncode));

            } catch (Exception e) {
                Logger.error("[{}] {}, 수집 실패 exception e={}", name, country, e.getMessage());
                continue;
            }
        }

        return requestMethodPostList;
    }

    public Response createStoredData(List<?> list, String cronAt, String name) {

        if (isEmpty(list)) {
            Logger.error("[{}] 수집 실패 parameter is null", name);
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
                JSONObject jsonObject = (JSONObject) item.getData();
                String currency = String.valueOf(jsonObject.get("pCurr"));
                String sRecvAmount = String.valueOf(jsonObject.get("pAmt"));
                float recvAmount = Float.parseFloat(sRecvAmount.replace(",",""));

                selenium.setCronAt(cronAt);
                selenium.setCompany(name.toLowerCase());
                selenium.setCountry(country);
                selenium.setCurrency(currency);
                selenium.setPrice(String.format("%.8f", recvAmount));

            } catch (Exception e) {
                Logger.error("[{}] {}, 수집 실패 exception e={}", name, country, e.getMessage());
                continue;
            }

            response.add(selenium);
        }

        return new Response(RESPONSE_SUCCESS.CODE(), "success", response, name);
    }

    @Override
    public Map<String, String> getHeaders(ChromeDriver chromeDriver, String url) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "en-US,en;q=0.9,ko;q=0.8");
        headers.put("Connection", "keep-alive");
        headers.put("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Host", "");// insert into Host
        headers.put("Origin", "");// insert into Origin
        headers.put("Referer", "");// insert into Referer
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Mode", "same-origin");
        headers.put("Sec-Fetch-Site", "XMLHttpRequest");

        return headers;
    }


    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}
}
