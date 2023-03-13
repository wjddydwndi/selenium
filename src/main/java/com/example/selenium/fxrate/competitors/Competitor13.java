package com.example.selenium.fxrate.competitors;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.common.util.CommonUtils;
import com.example.selenium.fxrate.AbstractCompetitor;
import com.example.selenium.model.Response;
import com.example.selenium.model.selenium.RequestParam;
import com.example.selenium.model.selenium.Selenium;
import com.example.selenium.service.selenium.SeleniumConfigService;
import com.google.api.client.util.Value;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
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
import java.util.concurrent.ConcurrentHashMap;

import static com.example.selenium.common.util.Commons.COMPETITOR13;
import static com.example.selenium.common.util.Commons.RESPONSE_SUCCESS;

@Component
@RequiredArgsConstructor
public class Competitor13 extends AbstractCompetitor {

    @Value("${competitors.competitor13}")
    private String url;
    private final String name = COMPETITOR13.CODE();
    private ChromeDriver chromeDriver;
    private final SeleniumConfigService seleniumConfigService;

    @Override
    public Response call() {
        return super.call(name, url);
    }

    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {

        LocalDateTime now = super.getCronAt();
        String cronAt = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.chromeDriver = chromeDriver;

        // 1. create Request Data
        List<Response> responseList = (List<Response>) createRequestData();

        // 3. Processing stored data
        return createStoredData(responseList, cronAt, name);
    }


    @Override
    public List<?> createRequestData() throws IOException, NullPointerException, ParseException {

        Map<String, String> headers = getHeaders(chromeDriver, url);
        List<Response> responseList = new ArrayList<>();

        JSONObject jsonObject = requestDataByGET(name, "", headers);//requestUrl
        JSONObject jsonData = (JSONObject) jsonObject.get("data");
        JSONObject jsonExrate = (JSONObject) jsonData.get("exRates");

        JSONObject jsonObject2 = requestDataByGET(name, "", headers);//requestURl2
        JSONObject jsonCountryInfo = (JSONObject) jsonObject2.get("data");

        Map<String, JSONObject> data = new ConcurrentHashMap<>();
        data.put("jsonExrate", jsonExrate);
        data.put("jsonCountryInfo", jsonCountryInfo);
        responseList.add(new Response(RESPONSE_SUCCESS.CODE(), "success", data, name));

        return responseList;
    }

    @Override
    public Response createStoredData(List<?> list, String cronAt, String name) throws NullPointerException {

        if (CommonUtils.isEmpty(list)) {
            Logger.error("[{}] 수집 실패 parameter is null", name);
            throw new NullPointerException();
        }

        List<Selenium> response = new ArrayList<>();
        List<Selenium> fail = new ArrayList<>();
        List<RequestParam> parameters = seleniumConfigService.getParameters(name);
        List<Response> responseList = (List<Response>) list;

        Response data = responseList.get(0);
        Map<String, JSONObject> dataMap = (Map<String, JSONObject>) data.getData();
        JSONObject jsonExrate = dataMap.get("jsonExrate");
        JSONObject jsonCountryInfo = dataMap.get("jsonCountryInfo");

        for (RequestParam item : parameters) {

            String country = item.getCountry();
            String currency = item.getCurrency();
            String countryEnCode = item.getCountryEnCode();
            String strSendAmount = item.getSendAmount();

            try {
                JSONArray array = (JSONArray) jsonExrate.get(countryEnCode.toUpperCase());
                JSONObject obj = (JSONObject) array.get(0);
                JSONObject wbRateData = (JSONObject) obj.get("wbRateData");

                JSONObject currencies = (JSONObject)jsonCountryInfo.get("currencies");
                JSONObject currencyObj = (JSONObject) currencies.get(currency);
                long scale = (long) currencyObj.get("scale");

                float sendAmount = Float.parseFloat(strSendAmount);
                float exchangeRate = calculator(sendAmount, wbRateData);
                int iScale = getScale(scale);

                float recvAmount = Math.round((sendAmount * exchangeRate) * iScale) / iScale;

                Selenium selenium = new Selenium();
                selenium.setCronAt(cronAt);
                selenium.setCompany(name.toLowerCase());
                selenium.setCountry(country);
                selenium.setCurrency(currency);
                selenium.setPrice(String.format("%.8f",recvAmount));

                response.add(selenium);

            } catch (Exception e) {
                Logger.error("[{}] {}, 수집 실패 e={}", name, e.getMessage());
                fail.add(new Selenium(cronAt, name, country));
                continue;
            }
        }

        Map<String, Object> responseData = new ConcurrentHashMap<>();
        responseData.put("success", response);
        responseData.put("fail", fail);

        return new Response(RESPONSE_SUCCESS.CODE(), "success", responseData, name);
    }

    @Override
    public Map<String, String> getHeaders(ChromeDriver chromeDriver, String url) {

        Map<String, String> headers = new HashMap<>();
        headers.put("apiVer", "1");
        headers.put("lang", "ko");

        return headers;
    }

    private float calculator(float sendAmount, JSONObject obj) {

        String strIdx = "";
        float value = 0;
        float compare = sendAmount;

        for (int i = 0; i < 7; i++) {

            double wbRate = (double) obj.get("wbRate" + strIdx);
            double threshold = (double) obj.get("threshold" + strIdx);

            if (CommonUtils.isEmpty(threshold) || compare > sendAmount) {
                break;
            }

            compare = (float) threshold;
            value = (float) wbRate;
            strIdx = String.valueOf(i + 1);
        }

        return value;
    }

    private int getScale(long scale) {
        int iScale = 10;
        for (int i = 1; i < scale; i++) {
            iScale *= 10;
        }
        return iScale;
    }

    @Override
    public Response request() throws IOException, ParseException, NullPointerException {return null;}
}
