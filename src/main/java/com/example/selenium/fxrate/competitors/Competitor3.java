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
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.Cookie;
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
public class Competitor3 extends AbstractCompetitor {

    @Value("${competitors.competitor3}")
    private String url;

    private final String name = COMPETITOR3.CODE();
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
        List<RequestMethodGet> list = (List<RequestMethodGet>) createRequestData();

        // 2. Thread call - Data collection
        List<Response> responseList = threadRun(name, list);

        // 3. Processing stored data
        return createStoredData(responseList, cronAt, name);
    }

    public List<?> createRequestData() {

        List<RequestParam> list = seleniumConfigService.getParameters(name);

        Map<String, String> headers = getHeaders(chromeDriver, url);
        List<RequestMethodPost> requestMethodPostList = new ArrayList<>();
        String requestUrl = "";// insert into requestUrl

        for (RequestParam item : list) {

            String country = item.getCountry();
            String currency = item.getCurrency();
            String countryCode = item.getCountryEnCode();
            String sendAmount = item.getSendAmount();

            Map<String, String> formData = new ConcurrentHashMap<>();
            formData.put("CRNCY_COD", "KRW");
            formData.put("DEFRAY_AMOUNT", sendAmount);
            formData.put("RCVER_EXPECT_NATN_COD", countryCode);
            formData.put("RCVER_EXPECT_CRNCY_COD", currency);
            formData.put("SIMULATION_YN", "Y");
            formData.put("OVSE_FEE_PROMOTION_YN", "N");
            formData.put("TOGGLE", "0");

            try {

                String urlEncode = urlEncode(formData);
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

                JSONObject data = new JSONObject(String.valueOf(jsonObject.get("data")));
                String sRecvAmount = String.valueOf(data.get("RCVER_EXPECT_RECPT_AMOUNT"));
                float recvAmount = Float.parseFloat(sRecvAmount);
                String currency = String.valueOf(data.get("RCVER_EXPECT_CRNCY_COD"));

                selenium.setCronAt(cronAt);
                selenium.setCompany(name.toLowerCase());
                selenium.setCountry(country);
                selenium.setCurrency(currency);
                selenium.setPrice(String.format("%.8f", recvAmount));

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

        Cookie jsessionid = chromeDriver.manage().getCookieNamed("JSESSIONID");

        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*/*");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "en-US,en;q=0.9,ko;q=0.8");
        headers.put("Connection", "keep-alive");
        headers.put("Content-type", "application/x-www-form-urlencoded; charset=UTF-8");
        headers.put("Cookie","JSESSIONID=" + jsessionid + "; __ga=GA1.3.46351579.1580345242; _gid=GA1.3.1042370538.1580345242; _gat_gtag_UA_111376381_1=1");
        headers.put("Host", "");// insert into Host
        headers.put("Origin", "");// insert into Origin
        headers.put("Referer", "");//insert into Referer
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-origin");
        headers.put("X-Requested-With", "XMLHttpRequest");

        return headers;
    }


    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}
}
