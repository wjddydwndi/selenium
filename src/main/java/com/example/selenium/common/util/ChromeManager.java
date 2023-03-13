package com.example.selenium.common.util;

import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class ChromeManager {

    public ChromeManager() {
        // webDriver 경로 설정
        System.setProperty("webdriver.chrome.driver", getDriverPath());
    }

    public ChromeDriver getWebDriver() {
        // 세션 시작
        ChromeOptions options = new ChromeOptions();
        // 페이지 로드 대기 (Normal : 로드 이벤트 실행이 반환될 때까지 대기)
        options.setPageLoadStrategy(PageLoadStrategy.NORMAL);
        return new ChromeDriver(options);
    }

    public ChromeDriver getDriver(String url)  {
        ChromeDriver chromeDriver = getWebDriver();
        chromeDriver.get(url);
        return chromeDriver;
    }

    public String getDriverPath() {

        String os = System.getProperty("os.name");

        String path = "D:\\selenium\\src\\main\\java\\com\\example\\selenium\\common\\util\\chromedriver.exe";
        if (os.indexOf("nux") > -1 || os.indexOf("nix") > -1 || os.indexOf("aix") > -1) {
            path = ChromeManager.class.getResource("").getPath().concat("chromedriver.exe").replace("target/classes", "src/main/java");
        }

        return path;
    }

    public static void wait(ChromeDriver chromeDriver, long amount, ChronoUnit unit) {
        Duration duration = Duration.of(amount, unit);
        chromeDriver.manage().timeouts().implicitlyWait(duration);
    }

    public static void close(ChromeDriver chromeDriver) {chromeDriver.close();}
}
