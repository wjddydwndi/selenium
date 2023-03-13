package com.example.selenium.common.util;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.model.selenium.SeleniumConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class CommonUtils {

    public static boolean isEmpty(Object obj) {

        if (obj instanceof String) {
            return obj == null || "".equals(obj.toString().trim());
        } else if (obj instanceof List) {
            return obj == null || ((List<?>) obj).isEmpty();
        } else if (obj instanceof Map) {
            return obj == null || ((Map<?, ?>) obj).isEmpty();
        } else if (obj instanceof Object[]) {
            return obj == null || Array.getLength(obj) == 0;
        } else {
            return obj == null;
        }
    }

    public static List<SeleniumConfig> getConfig(List<SeleniumConfig> configList, String category, String code) {

        if (isEmpty(configList) || isEmpty(category)) {
            return configList;
        }

        if (isEmpty(code)) {
            return configList.stream().filter(x-> x.getCategory().equals(category)).collect(Collectors.toList());
        }

        return configList.stream().filter(x-> x.getCategory().equals(category) && x.getCode().equals(code)).collect(Collectors.toList());
    }

    public static List<SeleniumConfig> getConfig(List<SeleniumConfig> configList, String category) {

        if (isEmpty(configList) || isEmpty(category)) {
            return configList;
        }

        return configList.stream().filter(x-> x.getCategory().equals(category)).collect(Collectors.toList());
    }

    public static List<SeleniumConfig> getConfig(List<SeleniumConfig> configList, String category, String codeParam, boolean enable) {

        if (isEmpty(configList) || isEmpty(category)) {
            return configList;
        }

        return configList.stream().filter(x-> x.getCategory().equals(category) && x.getCodeParam().equalsIgnoreCase(codeParam)).collect(Collectors.toList());
    }


    public static String keyboardEvent(String elementId, String key) {
        StringBuffer sb = new StringBuffer();
        sb.append("var event = document.createEvent(\"Events\");");
        sb.append("event.initEvent('keydown', true, true);");
        sb.append("event.keyCode ="+ convertKeyCode(key) + ";");
        sb.append("document.getElementById('"+elementId+"').dispatchEvent(event);");
        return sb.toString();
    }

    public static String convertKeyCode(String key) {
        int code = 0;

        int target = Integer.parseInt(key);

        switch (target) {
            case 0 : code = 96; break;
            case 1 : code = 97; break;
            case 2 : code = 98; break;
            case 3 : code = 99; break;
            case 4 : code = 100; break;
            case 5 : code = 101; break;
            case 6 : code = 102; break;
            case 7 : code = 103; break;
            case 8 : code = 104; break;
            case 9 : code = 105; break;
            case 11: code = 13; break;
        }

        return String.valueOf(code);
    }

    public static String floatToString(float fAmt) {
        return String.format("%.8f", fAmt);
    }

    public static String getItem(List<SeleniumConfig> list, String code, String codeParam) {

        String result = "";

        if (isEmpty(list) || isEmpty(code) || isEmpty(codeParam)) {
            return "";
        }

        for (SeleniumConfig item : list) {

            if (item.getCodeParam().equalsIgnoreCase(codeParam) && item.getCode().equalsIgnoreCase(code)) {
                result = item.getCodeValue();
                break;
            }

            if (item.getCode().equalsIgnoreCase(code) && item.getCodeParam().equalsIgnoreCase(Commons.CONFIG_CODE_PARAM_COMMON.CODE())) {
                result = item.getCodeValue();
                break;
            }
        }

        return result;
    }

    public static boolean isJsonValid(String str) {

        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(str);
            return true;
        } catch (IOException e) {

            return false;
        }
    }

    private static void getBits(StringBuffer sb, byte b) {

        for (int i = 0; i < 8; i++) {
            sb.append((b & 128) == 0 ? 0 : 1);
            b <<= 1;
        }
        sb.append(' ');
    }

    public static String toBinary(String s) {
        byte[] bytes = s.getBytes();
        StringBuffer sb = new StringBuffer();

        for (byte b : bytes) {
            getBits(sb, b);
        }

        return sb.toString().trim();
    }

    public static JSONObject parseStrToJson(String str) {
        if (isEmpty(str)) {
            return null;
        }

        try {
            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(str);

        } catch (Exception e) {
            e.printStackTrace();
            Logger.error("json parser error e={}", e.getMessage());
            return null;
        }
    }
}
