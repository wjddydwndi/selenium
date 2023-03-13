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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.selenium.common.util.CommonUtils.isEmpty;
import static com.example.selenium.common.util.Commons.*;

@Component
@RequiredArgsConstructor
public class Competitor5 extends AbstractCompetitor {
    @Value("${competitors.competitor5}")
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
        List<RequestMethodGet> RequestMethodGetList = (List<RequestMethodGet>) createRequestData();

        // 2. Thread call - Data collection
        List<Response> list = threadRun(name, RequestMethodGetList);

        // 3. Processing stored data
        return createStoredData(list, cronAt, name);
    }

    @Override
    public Map<String, String> getHeaders(ChromeDriver chromeDriver, String url) {return null;}

    @Override
    public List<?> createRequestData() {

        List<RequestParam> list = seleniumConfigService.getParameters(name);
        String requestUrl = url;

        List<RequestMethodGet> requestDataList = new ArrayList<>();

        for (RequestParam item : list) {

            String country = item.getCountry();
            String currency = item.getCurrency();
            String sendAmount = item.getSendAmount();
            String paymentType = item.getPaymentType();

            Map<String, String> formData = new ConcurrentHashMap<>();
            // 첫글자 대문자로
            String capitalizeStr = country.substring(0, 1).toUpperCase().concat(country.substring(1).toLowerCase());

            formData.put("receive_amount", "");
            formData.put("payout_country", capitalizeStr);
            formData.put("total_collected", sendAmount);
            formData.put("currencyType", currency);
            formData.put("payment_type", paymentType);

            try {

                String urlEncode = urlEncode(requestUrl, formData);
                requestDataList.add(new RequestMethodGet(name, country, urlEncode));

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
                String data = String.valueOf(item.getData());

                String[] dataArr
                        = data.replace("--tr_end--", "")
                        .replace("--td_clm--", "")
                        .replace("--td_end--", ",")
                        .split(",");


                Map<String, String> map = new HashMap<>();
                map.put("country", country);
                for (String s : dataArr) {

                    if (s.contains("serviceCharge")) {
                        continue;
                    } else if (s.contains("exchangeRate")) {
                        map.put("exchangeRate", s.substring("exchangeRate".length()));
                    } else if (s.contains("sendAmount")) {
                        map.put("sendAmount", s.substring("sendAmount".length()));
                    } else if (s.contains("receiveAmount")) {
                        map.put("receiveAmount", s.substring("receiveAmount".length()));
                    } else if (s.contains("receiveCType")) {
                        map.put("receiveCType", s.substring("receiveCType".length()));
                    }
                }

                String sReceiveAmount = map.get("receiveAmount");
                String sExchangeRate = map.get("exchangeRate");
                String sSendAmount = map.get("sendAmount");
                String receiveCType = map.get("receiveCType");

                if (isEmpty(receiveCType)) {
                    Logger.error("country={}, sReceiveAmount={}, sExchangeRate={}, sSendAmount={}, receiveCType={}", country, sReceiveAmount, sExchangeRate, sSendAmount, receiveCType);
                    continue;
                }

                float recvAmount = 0;
                if (!isEmpty(sReceiveAmount)) {
                    recvAmount = Float.parseFloat(sReceiveAmount);

                } else if (!isEmpty(sExchangeRate)) {
                    float exchangeRate = Float.parseFloat(sExchangeRate);
                    float sendAmount = Float.parseFloat(sSendAmount);
                    recvAmount = exchangeRate * sendAmount;

                } else {
                    Logger.error("country={}, sReceiveAmount={}, sExchangeRate={}, sSendAmount={}, receiveCType={}", country, sReceiveAmount, sExchangeRate, sSendAmount, receiveCType);
                    continue;
                }

                selenium.setCronAt(cronAt);
                selenium.setCompany(name.toLowerCase());
                selenium.setCountry(country);
                selenium.setCurrency(receiveCType);
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
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}
}
