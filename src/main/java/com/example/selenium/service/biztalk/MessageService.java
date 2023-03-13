package com.example.selenium.service.biztalk;

import com.example.selenium.model.selenium.Selenium;

import java.util.List;

public interface MessageService {
    void sendMessage(String name, List<Selenium> fail);
    void sendMessage(String name, int size, String content);
    void sendMessage(String name, String content);
    void sendSms(String from, String to, String title, String content);
    void sendMail(String from, String to, String title, String content, String[] cc);
}
