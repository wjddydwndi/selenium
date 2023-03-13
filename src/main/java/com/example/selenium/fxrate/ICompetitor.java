package com.example.selenium.fxrate;

import com.example.selenium.model.Response;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

public interface ICompetitor extends Callable {
    Response call();
    Response request() throws IOException, ParseException, NullPointerException;
    Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException;
    List<?> createRequestData() throws IOException, ParseException, java.text.ParseException;
    Response createStoredData(List<?> list, String cronAt, String name) throws NullPointerException;

}
