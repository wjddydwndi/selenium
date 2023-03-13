package com.example.selenium.model.selenium;

import lombok.Data;

/**
 * 경쟁사 크롤링에 필요한 파라미터를 담는다.
 * **/
@Data
public class RequestParam {

    private String country;
    private String company;
    private String currency;
    private String koCountryName;
    private String sendAmount;
    private String countryEnCode;
    private String deliveryMethod;
    private String paymentType;
    private String centerCode;
    private String countryNoCode;
    private String currencyNoCode;

}
