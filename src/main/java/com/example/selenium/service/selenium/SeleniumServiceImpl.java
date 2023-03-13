package com.example.selenium.service.selenium;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.common.thread.ThreadManager;
import com.example.selenium.common.util.CommonUtils;
import com.example.selenium.common.util.Commons;
import com.example.selenium.fxrate.ICompetitor;
import com.example.selenium.model.Response;
import com.example.selenium.model.selenium.Selenium;
import com.example.selenium.repository.selenium.SeleniumMapper;
import com.example.selenium.service.biztalk.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class SeleniumServiceImpl implements SeleniumService {

    private final List<ICompetitor> competitors;
    private final SeleniumMapper seleniumMapper;

    private final MessageService messageService;

    @Override
    public void request() {

        List<Response> list = search();

        List<Selenium> seleniums = manufactureData(list);

        insertByBulk(seleniums);

    }

    @Override
    public List<Response> search() {

        ThreadManager threadManager = null;
        List<Response> list = null;

        try {

            threadManager = new ThreadManager(competitors);

            list = threadManager.call();

        } catch (InterruptedException interruptedException) {
            Logger.error("크롤링 처리중 예외 발생 interruptedException = {}", interruptedException.getMessage());

        } catch (ExecutionException executionException) {
            Logger.error("크롤링 처리중 예외 발생 executionException = {}", executionException.getMessage());

        } catch (NullPointerException nullPointerException){
            Logger.error("크롤링 처리중 예외 발생 nullPointerException = {}", nullPointerException.getMessage());

        } catch (Exception e) {
            Logger.error("크롤링 처리중 예외 발생 e = {}", e.getMessage());

        } finally {

            if (!CommonUtils.isEmpty(threadManager)) {
                threadManager.shutdownNow();
            }
        }

        return list;
    }

    public synchronized List<Selenium> manufactureData(List<Response> list) {

        List<Selenium> response = new LinkedList<>();

        if (!CommonUtils.isEmpty(list)) {

            for (Response item : list) {
                String name = item.getName();

                if (item.getCode().equalsIgnoreCase(Commons.RESPONSE_SUCCESS.CODE())) {
                    Map<String, Object> data = (Map<String, Object>) item.getData();
                    List<Selenium> success = (List<Selenium>) data.get("success");
                    List<Selenium> fail = (List<Selenium>) data.get("fail");

                    Logger.info(">>>>> {} : ", name);

                    StringBuffer sb = new StringBuffer();
                    fail.stream().map(x -> x.getCountry()).forEach(x -> {
                        sb.append(x).append(", ");
                    });
                    sb.delete(sb.length() - 1, sb.length());

                    Logger.info("실패 항목 : {}", sb.toString());
                    messageService.sendMessage(name, fail.size(), sb.toString());

                } else {
                    Logger.info(">>>>> {} 수집 실패: ", name);

                    String s = name + " 수집 실패";
                    messageService.sendMessage(name, s);
                }
            }
        } else {
            throw new NullPointerException("응답값이 없음.");
        }

        return response;
    }

    @Override
    public void insertByBulk(List<Selenium> list) {seleniumMapper.inserByBulk(list);}


}

