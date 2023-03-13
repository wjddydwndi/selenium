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
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;
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
public class Competitor9 extends AbstractCompetitor {

    @Value("${competitors.competitor9}")
    private String url;
    private final String name = COMPETITOR9.CODE();
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
        List<Response> responseList = threadRun(COMPETITOR9.CODE(), list);

        // 3. Processing stored data
        return createStoredData(responseList, cronAt, COMPETITOR9.CODE());
    }

    public List<?> createRequestData() {

        List<RequestParam> list = seleniumConfigService.getParameters(COMPETITOR9.CODE());

        Map<String, String> headers = getHeaders(chromeDriver, url);
        List<RequestMethodPost> requestDataList = new ArrayList<>();
        String requestUrl = "";// requestUrl

        for (RequestParam item : list) {

            String country = item.getCountry();
            String currency = item.getCurrency();
            String sendAmount = item.getSendAmount();

            Map<String, String> formData = new ConcurrentHashMap<>();
            formData.put("send-unit", "KRW");
            formData.put("coupon-amount", "0");
            formData.put("send-amount", sendAmount);
            formData.put("receive-unit", currency);

            try {
                String urlEncode = urlEncode(formData);
                requestDataList.add(new RequestMethodPost(COMPETITOR8.CODE(), country, requestUrl, headers, urlEncode));

            } catch (Exception e) {
                Logger.error("[{}] {}, 수집 실패 exception e={}", COMPETITOR8.CODE(), country, e.getMessage());
                continue;
            }
        }

        return requestDataList;
    }

    public Response createStoredData(List<?> list, String cronAt, String name) {

        if (isEmpty(list)) {
            Logger.error("[{}] 수집 실패 parameter is null", COMPETITOR9.CODE());
            return new Response(RESPONSE_FAIL.CODE(), "fail", null, name);
        }

        List<RequestParam> parameters = seleniumConfigService.getParameters(COMPETITOR9.CODE());
        List<Selenium> response = new ArrayList<>();
        List<Response> responseList = (List<Response>) list;

        for (Response item : responseList) {

            String country = isEmpty(item.getCountry()) ? "" : item.getCountry();
            if (!item.getCode().equals(RESPONSE_SUCCESS.CODE())) {
                Logger.info("{}, 수집 실패 country={}", country);
                continue;
            }

            Optional<RequestParam> optional = parameters.stream().filter(x-> x.getCountry().equalsIgnoreCase(country)).findAny();

            if (!optional.isPresent()) continue;

            Selenium selenium = new Selenium();

            try {
                JSONObject jsonObject = (JSONObject) item.getData();

                JSONObject data = new JSONObject(String.valueOf(jsonObject.get("data")));
                JSONArray arrays = (JSONArray) data.get("receiveAmounts");
                JSONObject object = (JSONObject) arrays.get(0);

                double receiveAmount = (double) object.get("receive_amount");
                float recvAmount = (float) (Math.floor(receiveAmount * 100) / 100);

                RequestParam parameter = optional.get();

                selenium.setCronAt(cronAt);
                selenium.setCompany(COMPETITOR9.CODE().toLowerCase());
                selenium.setCountry(country);
                selenium.setCurrency(parameter.getCurrency());
                selenium.setPrice(String.format("%.8f", recvAmount));

            } catch (Exception e) {
                Logger.error("[{}] {} 수집 실패, exception e={}, ", COMPETITOR9.CODE(), country, e.getMessage());
                continue;
            }

            response.add(selenium);
        }

        return new Response(RESPONSE_SUCCESS.CODE(), "success", response, name);
    }


    @Override
    public Map<String, String> getHeaders(ChromeDriver chromeDriver, String url) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        return headers;
    }


    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}
}
