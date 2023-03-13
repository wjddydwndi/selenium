package com.example.selenium.service.biztalk;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.model.biztalk.ImcMtMsg;
import com.example.selenium.model.selenium.Selenium;
import com.example.selenium.repository.biztalk.MessageMapper;
import com.example.selenium.service.selenium.SeleniumConfigService;
import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static com.example.selenium.common.util.CommonUtils.isEmpty;
import static com.example.selenium.common.util.Commons.CONFIG_CODE_MAIL;
import static com.example.selenium.common.util.Commons.CONFIG_CODE_SMS;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    @Value("spring.sendgrid.sender")
    private String mailSender;

    @Value("spring.sendgrid.receiver")
    private String mailReceiver;

    @Value("sms.sender")
    private String smsSender;

    @Value("sms.receiver")
    private String smsReceiver;

    private final SendGrid sendGrid;
    private final MessageMapper messageMapper;

    private final SeleniumConfigService seleniumConfigService;



    public void sendMessage(String name, List<Selenium> fail) {

        if (isEmpty(name) || isEmpty(fail) || fail.size() < 1) {
            return;
        }

        Map<String, Object> target = seleniumConfigService.getMessageTarget();
        String[] sms = (String[]) target.get(CONFIG_CODE_SMS.CODE());
        String[] mail = (String[]) target.get(CONFIG_CODE_MAIL.CODE());

        String title = "[ "+ name +" ] 환율 수집 실패 ( "+fail.size()+" )\n";
        StringBuffer content = new StringBuffer();

        for (int i = 0; i < fail.size(); i++) {

            Selenium item = fail.get(i);
            String country = item.getCountry();

            if (i < fail.size()-1) {
                content.append(country).append(", ");
            } else {
                content.append(country);
            }
        }

        for (String s : sms) {
            sendSms(smsSender, s, title, content.toString());
        }

        sendMail(mailSender, mailSender, title, content.toString(), mail);
    }

    public void sendMessage(String name, int size, String content) {

        if (isEmpty(name) || isEmpty(size) || size < 1) {
            return;
        }

        Map<String, Object> target = seleniumConfigService.getMessageTarget();
        String[] sms = (String[]) target.get(CONFIG_CODE_SMS.CODE());
        String[] mail = (String[]) target.get(CONFIG_CODE_MAIL.CODE());

        String title = "[ "+ name +" ] 환율 수집 실패 ( "+ size +" )\n";

        for (String s : sms) {
            sendSms(smsSender, s, title, content);
        }

        sendMail(mailSender, mailSender, title, content, mail);
    }

    public void sendMessage(String name, String content) {

        if (isEmpty(name)) {
            return;
        }

        Map<String, Object> target = seleniumConfigService.getMessageTarget();
        String[] sms = (String[]) target.get(CONFIG_CODE_SMS.CODE());
        String[] mail = (String[]) target.get(CONFIG_CODE_MAIL.CODE());

        String title = "[ "+ name +" ] 환율 수집 실패\n";

        for (String s : sms) {
            sendSms(smsSender, s, title, content);
        }

        sendMail(mailSender, mailSender, title, content, mail);
    }

    @Override
    public void sendSms(String from, String to, String title, String content) {

        if (isEmpty(from)) {
            from = smsSender;
        }
        if (isEmpty(to)) {
            to = smsReceiver;
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDateTime reserviceDateTime = localDateTime.plusMinutes(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String now = localDateTime.format(formatter);
        String ReservedDate = reserviceDateTime.format(formatter);

        ImcMtMsg imcMtMsg = new ImcMtMsg();
        imcMtMsg.setMtType("LM");
        imcMtMsg.setStatus("1");
        imcMtMsg.setPriority("N");
        imcMtMsg.setReportDate(now);
        imcMtMsg.setReservedDate(ReservedDate);
        imcMtMsg.setPhoneNumber(to);
        imcMtMsg.setCallback(from);
        imcMtMsg.setTitle(title);
        imcMtMsg.setMessage(content);

        try {
            messageMapper.insert(imcMtMsg);
            Logger.info("문자발송 완료: RESERVED_DATE={}, PHONE_NUMBER={}, TITLE={}, MESSAGE={}",
                    imcMtMsg.getReservedDate(), imcMtMsg.getPhoneNumber(), imcMtMsg.getTitle(), imcMtMsg.getMessage());
        } catch (Exception e) {
            Logger.error("문자발송 에러 : RESERVED_DATE={}, PHONE_NUMBER={}, TITLE={}, MESSAGE={}",
                    imcMtMsg.getReservedDate(), imcMtMsg.getPhoneNumber(), imcMtMsg.getTitle(), imcMtMsg.getMessage(), e.getMessage());
        }

    }

    @Override
    public void sendMail(String from, String to, String title, String content, String[] cc) {

        if (isEmpty(from)) {
            from = mailSender;
        }
        if (isEmpty(to)) {
            to = mailReceiver;
        }

        SendGrid.Email email = new SendGrid.Email();
        email.addTo(to);
        email.setFromName("SBI Cosmoney < "+mailSender+">");
        email.setFrom(mailSender);
        email.setSubject(title);
        email.setHtml(content);
        email.addCc(cc);

        try {
            SendGrid.Response response = sendGrid.send(email);
            Logger.info("SendGrid 메일 발송 결과 : status={}, code={}, message={}", response.getStatus(), response.getCode(), response.getMessage());
        } catch (Exception e) {
            Logger.error("SendGrid 메일 발송 예외 발생 : to={}, fromName={}, from={}, subject={}, e={}"
                    , email.getTos(), email.getFromName(), email.getFrom(), email.getSubject(), e.getMessage());
        }
    }
}
