package com.example.selenium.service.selenium;

import com.example.selenium.model.selenium.RequestParam;
import com.example.selenium.model.selenium.SeleniumConfig;

import java.util.List;
import java.util.Map;

public interface SeleniumConfigService {

    void loadConfigAll();

    List<SeleniumConfig> selectSeleniumConfigAll();
    List<SeleniumConfig> getConfigAll();
    List<RequestParam> getParameters(String name);

    Map<String, Object> getMessageTarget();
}
