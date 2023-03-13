package com.example.selenium.repository.selenium;


import com.example.selenium.model.selenium.Selenium;

import java.util.List;


public interface SeleniumMapper {
    void inserByBulk(List<Selenium> list);
}
