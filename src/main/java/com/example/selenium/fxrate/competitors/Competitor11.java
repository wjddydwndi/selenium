package com.example.selenium.fxrate.competitors;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.common.util.AES;
import com.example.selenium.common.util.CryptoUtils;
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
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
public class Competitor11 extends AbstractCompetitor {
    @Value("${competitors.competitor11}")
    private String url;
    private final String name = COMPETITOR11.CODE();
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
        List<Response> responseList = threadRun(COMPETITOR11.CODE(), list);

        // 3. Processing stored data
        return createStoredData(responseList, cronAt, COMPETITOR11.CODE());
    }

    @Override
    public List<?> createRequestData() {

        List<RequestParam> list = seleniumConfigService.getParameters(COMPETITOR11.CODE());

        Map<String, String> headers = getHeaders(chromeDriver, url);
        List<RequestMethodPost> requestDataList = new ArrayList<>();
        String requestUrl = "";// requestUrl
        String secret = "";// secret

        int keySize = 256;
        int ivSize = 128;
        String salt = CryptoUtils.createSalt(8);
        byte[] bytesSalt = salt.getBytes(StandardCharsets.UTF_8);


        for (RequestParam item : list) {

            String country = item.getCountry();
            String countryNoCode = item.getCountryNoCode();
            String currencyNoCode = item.getCurrencyNoCode();

            JSONObject jsonData = new JSONObject();
            jsonData.put("platform", 1);
            jsonData.put("pid", null);
            jsonData.put("country", Integer.parseInt(countryNoCode));
            jsonData.put("currency", Integer.parseInt(currencyNoCode));

            try {
                byte[] key = new byte[keySize / 8];
                byte[] iv = new byte[ivSize / 8];

                Map<String, byte[]> map = CryptoUtils.EvpKDF(secret.getBytes(StandardCharsets.UTF_8), keySize, ivSize, bytesSalt, key, iv);
                byte[] keyBytes = map.get("keyBytes");
                byte[] ivBytes = map.get("ivBytes");

                AES aes = new AES(keyBytes, ivBytes);
                String strData = jsonData.toJSONString();
                int dataLength = strData.length();

                int amountToPad = 16 - (dataLength % 16);

                amountToPad = amountToPad == 0 ? 16 : amountToPad;

                char pad = (char) amountToPad;
                String combineText = strData + pad * amountToPad;
                byte[] b = combineText.getBytes(StandardCharsets.UTF_8);
                Logger.info(new String(b));

                String encVal = aes.encrypt(combineText);
                String combine = "Salted__" + salt + encVal;
                byte[] b1 = combine.getBytes(StandardCharsets.UTF_8);
                String result = Base64.encodeBase64URLSafeString(b1).stripTrailing();
                Logger.info(result);

                JSONObject jsonData2 = new JSONObject();
                jsonData2.put("data", result);


                requestDataList.add(new RequestMethodPost(COMPETITOR11.CODE(), country, requestUrl, headers, jsonData2.toJSONString()));

            } catch (Exception e) {
                Logger.error("[{}] {}, 수집 실패 exception e={}", COMPETITOR11.CODE(), country, e.getMessage());
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

        List<RequestParam> parameters = seleniumConfigService.getParameters(COMPETITOR11.CODE());

        List<Response> responseList = (List<Response>) list;

        for (Response item : responseList) {

            String country = item.getCountry();
            if (!item.getCode().equals(RESPONSE_SUCCESS.CODE())) {
                Logger.info("{}, 수집 실패 country={}", country);
                continue;
            }

            Selenium selenium = new Selenium();

            try {

                org.json.JSONObject jsonObject = (org.json.JSONObject) item.getData();
                org.json.JSONObject object = (org.json.JSONObject) jsonObject.get("d");

                String serviceFee = String.valueOf(object.get("ServiceFee"));
                String customerRate = String.valueOf(object.get("customer_rate"));


                selenium.setCronAt(cronAt);
                selenium.setCompany(COMPETITOR11.CODE().toLowerCase());
                selenium.setCountry(country);


            } catch (Exception e) {
                Logger.error("[{}] {} 수집 실패, exception e={}, ", COMPETITOR11.CODE(), country, e.getMessage());
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
        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Accept-Encoding", "gzip, deflate, br");
        headers.put("Accept-Language", "en-US,en;q=0.9,ko;q=0.8");
        headers.put("api_version", "1.1");
        headers.put("Authorization", "Bearer undefined");
        headers.put("Connection", "keep-alive");
        headers.put("content-type", "application/json; charset-utf-8");
        headers.put("Host", ""); // host
        headers.put("locale", "2");
        headers.put("Origin", "");// origin
        headers.put("platform", "1");
        headers.put("Referer", "");//referer
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-site");
        headers.put("User-Agent", userAgent);

        return headers;
    }


    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}
}
