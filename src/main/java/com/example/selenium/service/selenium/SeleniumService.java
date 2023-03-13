package com.example.selenium.service.selenium;

import com.example.selenium.model.Response;
import com.example.selenium.model.selenium.Selenium;

import java.util.List;

public interface SeleniumService {

    void request();
    List<Response> search();
    void insertByBulk(List<Selenium> list);

    List<Selenium> manufactureData(List<Response> list);
}
