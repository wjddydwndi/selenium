package com.example.selenium.repository.selenium;

import com.example.selenium.model.selenium.SeleniumConfig;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeleniumConfigMapper {
    List<SeleniumConfig> selectSeleniumConfigAll();
}
