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
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.selenium.common.util.CommonUtils.isEmpty;
import static com.example.selenium.common.util.Commons.*;

@Component
@RequiredArgsConstructor
public class Competitor2 extends AbstractCompetitor {
    @Value("${competitors.competitor2}")
    private String url;

    private final String name = COMPETITOR2.CODE();
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
        List<Response> list = threadRun(name, requestMethodPostList);

        // 3. Processing stored data
        return createStoredData(list, cronAt, name);
    }


    @Override
    public List<?> createRequestData() {

        List<RequestParam> list = seleniumConfigService.getParameters(name);
        String requestUrl = "";// insert into requestUrl
        Map<String, String> headers = getHeaders(chromeDriver, url);

        List<RequestMethodPost> requestDataList = new ArrayList<>();

        for (RequestParam item : list) {

            String country = item.getCountry();
            String currency = item.getCurrency();
            String sendAmount = item.getSendAmount();

            Map<String, String> formData = new ConcurrentHashMap<>();
            formData.put("sendingAmount", sendAmount);
            formData.put("sendingCurrency", "KRW");
            formData.put("receivingCurrency", currency);

            try {

                String urlEncode = urlEncode(formData);
                requestDataList.add(new RequestMethodPost(name, country, requestUrl, headers, urlEncode));

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

                String toCurrency = String.valueOf(jsonObject.get("toCurrency"));
                String toAmount = String.valueOf(jsonObject.get("toAmount"));
                float recvAmount = Float.parseFloat(toAmount);

                selenium.setCronAt(cronAt);
                selenium.setCompany(name.toLowerCase());
                selenium.setCountry(country);
                selenium.setCurrency(toCurrency);
                selenium.setPrice(String.format("%.8f", recvAmount));

            } catch (Exception e) {
                Logger.error("[{}] {} 수집 실패, exception e={}, ", name, country, e.getMessage());
                continue;
            }

            response.add(selenium);
        }

        return new Response(RESPONSE_SUCCESS.CODE(), "success", response, name);
    }

    public Map<String, String> getHeaders(ChromeDriver chromeDriver, String url) {

        Cookie jsessionid = chromeDriver.manage().getCookieNamed("JSESSIONID");
        //Cookie jsessionid = chromeDriver.manage().getCookieNamed("X-CSRF-TOKEN");
        WebElement element = chromeDriver.findElement(By.name("_csrf"));
        String csrf = element.getAttribute("content");

        Object obj = chromeDriver.executeScript("return navigator.userAgent");
        String userAgent = String.valueOf(obj);

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/javascript, */*; q=0.01");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "en-US,en;q=0.9,ko;q=0.8");
        headers.put("Connection", "keep-alive");
        headers.put("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Cookie", "_ga=GA1.2.49553800.1672123320; _fbp=fb.1.1672123319815.511075502; _gid=GA1.2.1800237730.1676253178; lang_cookie=ko; JSESSIONID="+jsessionid);
        headers.put("Host", "coinshot.org");
        headers.put("Origin", "");// insert into Origin
        headers.put("Referer", "");// insert into Referer

        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("User-Agent", userAgent);
        headers.put("X-CSRF-TOKEN", csrf);
        headers.put("Content-Length", "63");
        headers.put("X-Requested-With", "XMLHttpRequest");

        return headers;
    }

    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}
}
