package com.example.selenium.common.thread;

import com.example.selenium.common.logger.Logger;
import com.example.selenium.model.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static com.example.selenium.common.util.CommonUtils.isEmpty;
import static com.example.selenium.common.util.Commons.RESPONSE_SUCCESS;

public class ThreadManager implements ThreadPool {

    private ExecutorService executorService;
    private List<Callable<Response>> tasks;

    public ThreadManager(List<?> list) {
        // 쓰레드 풀 관리
        executorService = Executors.newFixedThreadPool(5);
        tasks = new ArrayList<>();
        
        for (int i = 0; i < list.size(); i++) {
            Callable<Response> task = (Callable<Response>) list.get(i);
            tasks.add(task);
        }
    }

    @Override
    public List<Response> call() throws InterruptedException, ExecutionException, NullPointerException {

        List<Response> list = new ArrayList<>();

        try {

            List<Future<Response>> futureList = executorService.invokeAll(tasks);

            for (Future<Response> future : futureList) {

                if (future.isDone() && !isEmpty(future.get())) {
                    Response response = future.get();
                    // 성공
                    if (response.getCode().equals(RESPONSE_SUCCESS.CODE())) {
                        Logger.info("[Thread] name={}, country={}, code={}, message={}, data={}", response.getName(), response.getCountry(), response.getCode(), response.getMessage(), response.getData());
                    } else {// 에러
                        Logger.error("[Thread] name={}, country={}, code={}, message={}", response.getName(), response.getCountry(), response.getCode(), response.getMessage());
                    }
                    list.add(response);
                }
            }

        } catch (Exception e) {
            shutdownNow();
        } finally {
            if (isShutdown()) {
                shutdown();
            }
        }

        return list;
    }

    @Override
    public List<Response> asyncCall() throws InterruptedException, ExecutionException {

        List<Future<Response>> futureList = new ArrayList<>();
        List<Response> responseList = new ArrayList<>();

        for (Callable task : tasks) {
            futureList.add(executorService.submit(task));
        }

        for (Future<Response> future : futureList) {

            Response response = future.get();
            responseList.add(response);
        }

        shutdown();
        return responseList;
    }

    @Override
    public void shutdown() {executorService.shutdown();}

    @Override
    public List<Runnable> shutdownNow() {return executorService.shutdownNow();}

    @Override
    public boolean isShutdown() {return executorService.isShutdown();}

    @Override
    public boolean isTerminated() {return executorService.isTerminated();}

    @Override
    public boolean awaitTermination(long timeout) throws InterruptedException {

        if (isEmpty(timeout)) {
            Logger.error("parameters");
        }

        return executorService.awaitTermination(timeout, TimeUnit.SECONDS);
    }
}
