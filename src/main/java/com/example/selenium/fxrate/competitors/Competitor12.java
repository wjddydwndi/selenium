package com.example.selenium.fxrate.competitors;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.common.util.CommonUtils;
import com.example.selenium.common.util.Commons;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.selenium.common.util.Commons.COMPETITOR12;
import static com.example.selenium.common.util.Commons.RESPONSE_SUCCESS;


@Component
@RequiredArgsConstructor
public class Competitor12 extends AbstractCompetitor {

    @Value("${competitors.competitor12}")
    private String url;
    private final String name = COMPETITOR12.CODE();
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
    public List<?> createRequestData() throws NullPointerException, IOException, ParseException {

        List<RequestParam> list = seleniumConfigService.getParameters(name);

        Map<String, String> headers = getHeaders(chromeDriver, url);
        List<Response> responseList = new ArrayList<>();
        String requestUrl = "";

        JSONObject jsonObject = requestDataByGET(name, requestUrl);
        JSONArray array = (JSONArray) jsonObject.get("data");

        for (RequestParam item : list) {

            String country = item.getCountry();
            String currency = item.getCurrency();
            String sendAmount = item.getSendAmount();
            float fSendAmount = Float.parseFloat(sendAmount);

            try {
                Optional<JSONObject> optional = array.stream().filter(x -> String.valueOf(((JSONObject) x).get("code")).equalsIgnoreCase(currency)).findAny();
                if (!optional.isPresent()) {
                    throw new NullPointerException();
                }

                String sExRate = String.valueOf(optional.get().get("ex_rate"));
                float fExrate = Float.parseFloat(sExRate);
                float price = Math.round(fSendAmount * (1 / fExrate) * 100) / 100;

                Map<String, Object> data = new ConcurrentHashMap<>();

                data.put("country", country);
                data.put("currency", currency);
                data.put("price", price);

                responseList.add(new Response(RESPONSE_SUCCESS.CODE(), "success", data, name, country));

            } catch (Exception e) {
                Logger.error("[{}] {}, 수집 실패 e={}", name, country, e.getMessage());
                continue;
            }
        }

        return responseList;

    }

    @Override
    public Response createStoredData(List<?> list, String cronAt, String name) {

        if (CommonUtils.isEmpty(list) || CommonUtils.isEmpty(cronAt) || CommonUtils.isEmpty(name)) {
            Logger.error("[{}] 수집 실패 parameter is null", name);
            throw new NullPointerException();
        }

        List<Selenium> response = new ArrayList<>();
        List<Selenium> fail = new ArrayList<>();
        List<Response> responseList = (List<Response>) list;

        for (Response item : responseList) {

            String country = CommonUtils.isEmpty(item.getCountry()) ? "" : item.getCountry();
            if (!item.getCode().equals(Commons.RESPONSE_SUCCESS.CODE())) {
                Logger.info("{}, 수집 실패 country={}", country);
                continue;
            }

            Selenium selenium = new Selenium();

            try {
                Map<String, Object> data = (Map<String, Object>) item.getData();
                String currency = String.valueOf(data.get("currency"));
                float price = Float.valueOf((Float) data.get("price"));

                selenium.setCronAt(cronAt);
                selenium.setCompany(name.toLowerCase());
                selenium.setCountry(country);
                selenium.setCurrency(currency);
                selenium.setPrice(String.format("%.8f", price));


            } catch (Exception e) {
                Logger.error("[{}] {} 수집 실패, exception e={}, ", name, country, e.getMessage());
                fail.add(new Selenium(cronAt, name, country));
                continue;
            }

            response.add(selenium);
        }

        Map<String, Object> responseData = new ConcurrentHashMap<>();
        responseData.put("success", response);
        responseData.put("fail", fail);

        return new Response(Commons.RESPONSE_SUCCESS.CODE(), "success", responseData, name);
    }

    @Override
    public Response request() throws IOException, ParseException, NullPointerException {return null;}

}
