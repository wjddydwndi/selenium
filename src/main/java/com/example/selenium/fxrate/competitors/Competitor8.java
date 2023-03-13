package com.example.selenium.fxrate.competitors;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.common.util.ChromeManager;
import com.example.selenium.fxrate.AbstractCompetitor;
import com.example.selenium.model.Response;
import com.example.selenium.model.selenium.RequestParam;
import com.example.selenium.model.selenium.Selenium;
import com.example.selenium.service.selenium.SeleniumConfigService;
import com.example.selenium.service.selenium.SeleniumService;
import com.google.api.client.util.Value;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.selenium.common.util.CommonUtils.floatToString;
import static com.example.selenium.common.util.Commons.COMPETITOR8;
import static com.example.selenium.common.util.Commons.RESPONSE_SUCCESS;


@Component
@RequiredArgsConstructor
public class Competitor8 extends AbstractCompetitor {
    @Value("${competitors.competitor8}")
    private String url;

    private final String name = COMPETITOR8.CODE();
    private ChromeDriver chromeDriver;
    private final SeleniumService seleniumService;
    private final SeleniumConfigService seleniumConfigService;

    @Override
    public Response call() {return super.call(name, url);}


    public Response request() throws IOException, ParseException, NullPointerException {

        List<RequestParam> list = seleniumConfigService.getParameters(name);
        List<Selenium> response = new ArrayList<>();

        try {
            LocalDateTime now = super.getCronAt();
            String strNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            Duration duration = Duration.of(20, ChronoUnit.SECONDS);
            WebDriverWait wait = new WebDriverWait(this.chromeDriver,  duration);
            JavascriptExecutor js = (JavascriptExecutor) this.chromeDriver;

            for (RequestParam item : list) {

                String country = item.getCountry();
                String currency = item.getCurrency();
                String sendAmount = item.getSendAmount();
                String koCountryName = item.getKoCountryName();

                StringBuffer sb = new StringBuffer();
                sb.append("var paramForm = JUtilForm.createForm('popFormNat');");
                sb.append("ExtLayerPop.load(paramForm, 'PBKFXR030000', 'PBKFXR0101000201V');");
                Object value = js.executeScript(sb.toString());
                ChromeManager.wait(this.chromeDriver, 10, ChronoUnit.SECONDS);
                Thread.sleep(1000);
                Object popupResult = js.executeScript(getScriptWithCountry(koCountryName));

                Thread.sleep(500);
                sb.delete(0, sb.length());
                sb.append("var fxrate = parseFloat($('#spCrncyVal').html().replace(/[^\\d.]/g, ''));");
                sb.append("var sendAmount = " + sendAmount + ";");
                sb.append("return Ceiling(sendAmount / fxrate, 2);");

                String strAmt = String.valueOf(js.executeScript(sb.toString()));
                float fAmt = Float.parseFloat(strAmt);

                Selenium selenium = new Selenium();
                selenium.setCronAt(strNow);
                selenium.setCompany(name.toLowerCase());
                selenium.setCurrency(currency);
                selenium.setCountry(country);
                selenium.setPrice(floatToString(fAmt));

                response.add(selenium);
            }
        } catch (Exception e) {
            e.getStackTrace();
            Logger.error("{}, exception e={}",name, e.getMessage());
        } finally {
            ChromeManager.close(this.chromeDriver);
        }

        return new Response(RESPONSE_SUCCESS.CODE(), "success", response, name);
    }

    private String getScriptWithCountry(String koCountry){
        StringBuffer sb = new StringBuffer();

        sb.append("var $frm = $('#contentForm'); ");
        sb.append("var targetCountry = $('#resultArea > li > a:contains(\"" + koCountry + "\")');");
        sb.append("$frm.find('#natId').val(targetCountry.find('#natId').val());");
        sb.append("$frm.find('#natNm').val(targetCountry.find('#natKorNm').val());");
        sb.append("$frm.find('#natEngNm').val(targetCountry.find('#natEngNm').val());");
        sb.append("$frm.find('#crncyId').val(targetCountry.find('#rmtncCrncyId').val());");
        sb.append("$frm.find('#crncyNm').val(targetCountry.find('#prmryUseCrncyNm').val());");
        sb.append("$frm.find('#crncyCdEngNm').val(targetCountry.find('#crncyCdEngNm').val());");
        sb.append("$frm.find('#ibanYn').val(targetCountry.find('#ibanYn').val());");
        sb.append("$frm.find('#crncy100UnitYn').val(targetCountry.find('#crncy100UnitYn').val());");
        sb.append("$frm.find('#ovrsRmtncRutnCdNm').val(targetCountry.find('#ovrsRmtncRutnCdNm').val());");
        sb.append("$frm.find('#ovrsRmtncRutnCdLen').val(targetCountry.find('#ovrsRmtncRutnCdLen').val());");
        sb.append("$frm.find('#ovrsRmtncAcctNbrMinLen').val(targetCountry.find('#ovrsRmtncAcctNbrMinLen').val());");
        sb.append("$frm.find('#ovrsRmtncAcctNbrMaxLen').val(targetCountry.find('#ovrsRmtncAcctNbrMaxLen').val());");
        sb.append("var callbackFunc = window['selNatCallbackFunction'];");
        sb.append("if (typeof (callbackFunc) == 'function') {selNatCallbackFunction();}");
        sb.append("ExtLayerPop.close();");

        return sb.toString();

    }

    @Override
    public Map<String, String> getHeaders(ChromeDriver chromeDriver, String url) {
        return null;
    }


    @Override
    public Response request(ChromeDriver chromeDriver) throws IOException, ParseException, NullPointerException {return null;}
}
