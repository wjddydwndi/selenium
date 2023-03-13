package com.example.selenium.model.selenium;

import lombok.Data;

@Data
public class Selenium {


    public Selenium() {}

    public Selenium(long seq, String cronAt, String company, String currency, String price, String country, String usdCvsPrice, String spreadUsdCvsPrice) {
        this.seq = seq;
        this.cronAt = cronAt;
        this.company = company;
        this.currency = currency;
        this.price = price;
        this.country = country;
        this.usdCvsPrice = usdCvsPrice;
        this.spreadUsdCvsPrice = spreadUsdCvsPrice;
    }

    public Selenium(String cronAt, String company, String country) {
        this.cronAt = cronAt;
        this.company = company;
        this.country = country;
    }

    public Selenium(String cronAt, String company, String currency, String price, String country) {
        this.cronAt = cronAt;
        this.company = company;
        this.currency = currency;
        this.price = price;
        this.country = country;
    }
    private long seq;
    private String cronAt;
    private String company;
    private String currency;
    private String price;
    private String country;
    private String usdCvsPrice;
    private String spreadUsdCvsPrice;
}
