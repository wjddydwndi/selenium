package com.example.selenium.fxrate.competitors;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.common.util.ChromeManager;
import com.example.selenium.fxrate.AbstractCompetitor;
import com.example.selenium.fxrate.etc.RequestMethodGet;
import com.example.selenium.model.Response;
import com.example.selenium.model.selenium.RequestParam;
import com.example.selenium.model.selenium.Selenium;
import com.example.selenium.service.selenium.SeleniumConfigService;
import com.example.selenium.service.selenium.SeleniumService;
import com.google.api.client.util.Value;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.selenium.common.util.CommonUtils.isEmpty;
import static com.example.selenium.common.util.Commons.*;

@Component
@RequiredArgsConstructor
public class Competitor1 extends AbstractCompetitor {

    @Value("${competitors.competitor1}")
    private String url;
    private final String name = COMPETITOR1.CODE();
    private ChromeDriver chromeDriver;
    private final SeleniumService seleniumService;
    private final SeleniumConfigService seleniumConfigService;

    @Override
    public Response call() {return super.call(name, url);}

    @Override
    public Response request() throws IOException, ParseException, NullPointerException {
        return null;
    }

    @Override
    public Response request(ChromeDriver chromeDriver) {

        try {

            LocalDateTime now = super.getCronAt();
            String cronAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 1. create Request Data
            List<RequestMethodGet> list = (List<RequestMethodGet>) createRequestData();

            // 2. Processing stored data
            return createStoredData(list, cronAt, name);

        } catch (Exception e) {
            e.getStackTrace();
            Logger.error("{}, exception e={}", name, e.getMessage());
            return new Response(RESPONSE_FAIL.CODE(), "fail", null, name);
        } finally {
            ChromeManager.close(this.chromeDriver);
        }
    }

    @Override
    public List<?> createRequestData() {

        List<RequestParam> list = seleniumConfigService.getParameters(name);

        Map<String, String> headers = getHeaders(chromeDriver, url);
        String targetUrl = "";// insert into targetUrl

        List<RequestMethodGet> requestDataList = new ArrayList<>();
        JSONArray array = null;

        try {

            JSONObject json = sendGet(targetUrl, headers, null);
            JSONObject data = (JSONObject) json.get("data");
            array = (JSONArray) data.get("country_configure");

        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("[{}], 수집 실패 exception e={}", name, e.getMessage());
        }

            String reqeustUrl = ""; // insert into requestUrl

            for (int i = 0; i < array.size(); i++) {

                String country = "";

                try {
                    JSONObject obj = (JSONObject) array.get(i);
                    country = String.valueOf(obj.get("name")).toUpperCase();
                    String currency = String.valueOf(obj.get("currency"));

                    String finalCountry = country;
                    Optional<RequestParam> optional = list.stream().filter(x -> x.getCountry().equals(finalCountry) && x.getCurrency().equals(currency)).findAny();

                    if (optional.isPresent()) {
                        RequestParam requestParam = optional.get();
                        String platformId = String.valueOf(obj.get("platform_id"));

                        Map<String, String> formData = new ConcurrentHashMap<>();
                        formData.put("apply_user_limit", "0");
                        formData.put("deposit_type", "Manual");
                        formData.put("platform_id", platformId);
                        formData.put("quote_type", "send");
                        formData.put("sending_amount", requestParam.getSendAmount());
                        String urlEncode = urlEncode(reqeustUrl, formData);

                        requestDataList.add(new RequestMethodGet(name, country, urlEncode));
                    }

            } catch (Exception e) {
                e.printStackTrace();
                Logger.error("[{}] {}, 수집 실패 exception e={}", name, country, e.getMessage());
                continue;
            }
        }


        return requestDataList;
    }

    @Override
    public Response createStoredData(List<?> list, String cronAt, String name) {

        if (isEmpty(list)) {
            Logger.error("[{}] 수집 실패 parameter is null", name);
            return new Response(RESPONSE_FAIL.CODE(), "fail", null, name);
        }

        List<Selenium> response = new ArrayList<>();

        List<RequestMethodGet> requestMethodGetList = (List<RequestMethodGet>) list;

        for (RequestMethodGet item : requestMethodGetList) {

            String country ="";

            try {

                Response res = item.request(chromeDriver);

                country = res.getCountry();
                JSONObject jsonObject = (JSONObject) res.getData();
                JSONObject resData = (JSONObject) jsonObject.get("data");
                String currency = String.valueOf(resData.get("currency"));
                String sRecvAmount = String.valueOf(resData.get("receiving_amount"));
                float fReceivingAmount = Float.parseFloat(sRecvAmount);

                Selenium selenium = new Selenium();
                selenium.setCronAt(cronAt);
                selenium.setCompany(name.toLowerCase());
                selenium.setCountry(country);
                selenium.setCurrency(currency);
                selenium.setPrice(String.format("%.8f",fReceivingAmount));

                response.add(selenium);

            } catch (Exception e) {
                Logger.error("[{}] {} 수집 실패, exception e={}, ", name, country, e.getMessage());
                continue;
            }
        }

        return new Response(RESPONSE_SUCCESS.CODE(), "success", response, name);
    }

    public Map<String, String> getHeaders(ChromeDriver chromeDriver, String url) {

        Cookie cookie = chromeDriver.manage().getCookieNamed("csrftoken");
        Object obj = chromeDriver.executeScript("return navigator.userAgent");
        String userAgent = String.valueOf(obj);

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3");
        headers.put("Connection", "keep-alive");
        headers.put("Cache-Control", "max-age=0");
        headers.put("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Cookie", "csrftoken=" + cookie.getValue() + "; __ga=GA1.2.848184867.1580204307; _fbp=fb.1.1580204307582.277676330; lang=en; _gid=GA1.2.1660660160.1582594053; notice_modal=262,254");
        headers.put("Host", "");// insert into host
        headers.put("Referer", "");// insert into referer
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("User-Agent", userAgent);
        headers.put("User-Agent", userAgent);
        headers.put("X-CSRF-TOKEN", cookie.getValue());
        headers.put("X-Requested-With", "XMLHttpRequest");

        return headers;
    }
}
