package com.example.selenium.batch;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.service.selenium.SeleniumService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class SeleniumScheduler {

    private final SeleniumService seleniumService;

    @Scheduled(cron = "10 * * * * *")
    public void run() {

        Logger.info(">>>>>> selenium crawling start...");

        seleniumService.request();

        Logger.info("<<<<<< selenium crawling end...");
    }

}
