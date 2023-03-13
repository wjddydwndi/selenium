package com.example.selenium.fxrate;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.common.thread.ThreadManager;
import com.example.selenium.common.util.ChromeManager;
import com.example.selenium.fxrate.etc.RequestMethodGet;
import com.example.selenium.fxrate.etc.RequestMethodPost;
import com.example.selenium.model.Response;
import lombok.Cleanup;
import lombok.NonNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.chrome.ChromeDriver;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.example.selenium.common.util.CommonUtils.*;
import static com.example.selenium.common.util.Commons.RESPONSE_FAIL;
import static com.example.selenium.common.util.Commons.RESPONSE_SUCCESS;

public abstract class AbstractCompetitor implements ICompetitor, RequestUtil {

    private LocalDateTime cronAt;
    private int connectionTimeout = 30;



    public Response call(String name, String url) {

        Response response = null;
        ChromeDriver chromeDriver = null;

        try {
            chromeDriver = chromeOpen(url);

            response = request(chromeDriver);

        } catch (Exception e) {
            Logger.error("{}, call exception ={}", name, e.getMessage());
            return new Response(RESPONSE_FAIL.CODE(), "call exception", name);

        } finally {
            chromeClose(chromeDriver);
        }

        return response;
    }

    public JSONObject sendPost(@NonNull String targetUrl, @NonNull Map<String, String> headers, @NonNull String param) throws IOException {
        String response = sendPost(new URL(targetUrl), headers, param);

        JSONObject jsonObject = null;

        if (isJsonValid(response) == true) {
            jsonObject = parseStrToJson(response);
        }

        return jsonObject;
    }

    public String sendPost(@NonNull URL url, @NonNull Map<String, String> headers, @NonNull String param) throws IOException {

        //URL url = new URL(targetUrl);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        con.setConnectTimeout(connectionTimeout * 1000);
        con.setRequestMethod("POST");
        con.setDoOutput(true);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }

        OutputStream os = con.getOutputStream();

        try {
            os.write(param.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } catch (Exception e) {
            throw  e;
        } finally {
            os.close();
        }

        //@Cleanup IO 처리, jdbc 처리시 , close() 메소드 대신 해당 자원이 자동으로 닫히는 것이 보장
        @Cleanup
        BufferedReader in = getBufferReader(con.getInputStream());

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        return response.toString();
    }

    public JSONObject sendGet(@NonNull String targetUrl) throws IOException {

        JSONObject jsonObject = null;

        if (!isEmpty(targetUrl)) {
            targetUrl = targetUrl.concat("?");
        }

        String response = sendGet(new URL(targetUrl));


        if (isJsonValid(response)) {
            jsonObject = parseStrToJson(response);
        }

        return jsonObject;
    }

    public String sendGet(@NonNull URL url) throws IOException {

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        con.setConnectTimeout(connectionTimeout * 1000);
        con.setRequestMethod("GET");
        con.setDoOutput(true);

        @Cleanup BufferedReader in = getBufferReader(con.getInputStream());

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        return response.toString();
    }

    public JSONObject sendGet(@NonNull String targetUrl, @NonNull Map<String, String> headers, String param) throws IOException {

        if (!isEmpty(targetUrl)) {
            targetUrl = targetUrl.concat("?");
        }

        JSONObject jsonObject = null;

        String responose = sendGet(new URL(targetUrl), headers, param);

        if (isJsonValid(responose)) {
            jsonObject = parseStrToJson(responose);
        }

        return jsonObject;
    }

    public String sendGet(@NonNull URL url, @NonNull Map<String, String> headers, String param) throws IOException {

        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

        con.setConnectTimeout(connectionTimeout * 1000);
        con.setRequestMethod("GET");
        con.setDoOutput(true);

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            con.setRequestProperty(entry.getKey(), entry.getValue());
        }

        @Cleanup BufferedReader in = getBufferReader(con.getInputStream());

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        return response.toString();
    }

    public JSONObject requestDataByGET(String name, String requestUrl, Map<String, String> headers) throws IOException, ParseException, NullPointerException {

        RequestMethodGet requestMethodGet = new RequestMethodGet(name, requestUrl, headers);
        Response response = requestMethodGet.request();

        if (!response.getCode().equalsIgnoreCase(RESPONSE_SUCCESS.CODE())) {
            throw new NullPointerException();
        }

        return (JSONObject) response.getData();
    }

    public JSONObject requestDataByGET(String name, String requestUrl) throws IOException, ParseException, NullPointerException {

        RequestMethodGet requestMethodGet = new RequestMethodGet(name, requestUrl);
        Response response = requestMethodGet.request();

        if (!response.getCode().equalsIgnoreCase(RESPONSE_SUCCESS.CODE())) {
            throw new NullPointerException();
        }

        return (JSONObject) response.getData();
    }

    public JSONObject requestDataByPOST(String name, String requestUrl, Map<String, String> headers, String urlEncode) throws IOException, ParseException, NullPointerException {

        RequestMethodPost requestMethodPost = new RequestMethodPost(name, requestUrl, headers, urlEncode);
        Response response = requestMethodPost.request();

        if (!response.getCode().equalsIgnoreCase(RESPONSE_SUCCESS.CODE())) {
            throw new NullPointerException();
        }

        return (JSONObject) response.getData();
    }

    private BufferedReader getBufferReader(InputStream inputStream) throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
    }

    public String urlEncode(String url, Map<String, String> map) {return url.concat("?").concat(urlEncode(map));}

    public String urlEncode(Map<String, String> map) {

        if (isEmpty(map)) {
            return "";
        }

        StringBuffer sb = new StringBuffer();

        for (String key : map.keySet()) {

            String value = map.get(key);

            if (sb.toString().length() > 1) {
                sb.append("&");
            }

            try {
                sb.append(URLEncoder.encode(key, StandardCharsets.UTF_8)).append("=");
                sb.append(URLEncoder.encode(value, StandardCharsets.UTF_8));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public List<Response> threadRun(String name, List<?> list) {

        if (isEmpty(list)) {
            Logger.error("{}, parameter is null", name);
            return null;
        }

        List<Response> responseList = null;

        try {

            ThreadManager threadManager = new ThreadManager(list);

            responseList = threadManager.call();

        }  catch (ExecutionException e) {
            e.printStackTrace();
            Logger.error("{}, ExecutionException e={}", name, e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Logger.error("{}, InterruptedException e={}", name, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("{}, Exception e={}", name, e.getMessage());
        }

        return responseList;
    }

    @Override
    public List<?> createRequestData() throws IOException, ParseException, java.text.ParseException {return null;}
    @Override
    public Response createStoredData(List<?> list, String cronAt, String name) {return null;}
    @Override
    public Map<String, String> getHeaders(ChromeDriver chromeDriver, String url) {return null;}


    private ChromeDriver chromeOpen(String url) {

        ChromeDriver chromeDriver = null;

        if (isEmpty(url)) {
            return chromeDriver;
        }

        ChromeManager chromeManager = new ChromeManager();

        try {
            chromeDriver  = chromeManager.getDriver(url);
            chromeDriver.manage().window().maximize();// 브라우저 창 최대화

        } catch (Exception e) {
            Logger.error("페이지가 열리지 않아 예외 발생 e={}", e.getMessage());
            ChromeManager.close(chromeDriver);
        }

        return chromeDriver;
    }

    private void chromeClose(ChromeDriver chromeDriver) {

        if (!isEmpty(chromeDriver)) {
            chromeDriver.close();
        }
    }

    public void setCronAt(LocalDateTime cronAt) {this.cronAt = cronAt;}
    public LocalDateTime getCronAt() {return this.cronAt;}
}
