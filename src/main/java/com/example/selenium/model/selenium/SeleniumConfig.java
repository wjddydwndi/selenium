package com.example.selenium.model.selenium;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class SeleniumConfig {

    private int seq;
    private String category;
    private String code;
    private String codeValue;
    private String codeParam;
    private boolean enable;
    private String description;
    private Date createTime;
    private Date updateTime;

}
