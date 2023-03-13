package com.example.selenium.fxrate;

import lombok.NonNull;
import org.json.simple.JSONObject;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public interface RequestUtil {

    JSONObject sendPost(@NonNull String targetUrl, @NonNull Map<String, String> headers, @NonNull String param) throws IOException;
    JSONObject sendGet(@NonNull String targetUrl) throws IOException;
    JSONObject sendGet(@NonNull String targetUrl, @NonNull Map<String, String> headers, @NonNull String param) throws IOException;

    String sendPost(@NonNull URL url, @NonNull Map<String, String> headers, @NonNull String param) throws IOException;
    String sendGet(@NonNull URL url) throws IOException;
    String sendGet(@NonNull URL url, @NonNull Map<String, String> headers, @NonNull String param) throws IOException;
    String urlEncode(Map<String, String> map);
    Map<String, String> getHeaders(ChromeDriver chromeDriver, String url);
}
