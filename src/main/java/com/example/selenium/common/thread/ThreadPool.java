package com.example.selenium.common.thread;

import com.example.selenium.model.Response;

import java.util.List;
import java.util.concurrent.ExecutionException;


/**
 * Callable : return 값이 있음.
 * **/
public interface ThreadPool {

    /**
     * 여러 Callable 작업을 실행한다.
     * 모든 작업이 끝날 때까지 기다린다.
     * @return List<Reponse>
     * **/
    List<Response> call() throws InterruptedException, ExecutionException;


    /**
     * 여러 Callable 작업을 비동기 실행한다.
     * @return List<Reponse>
     * **/
    List<Response> asyncCall() throws InterruptedException, ExecutionException;


    /**
     * 이미 Executor에 제공된 작업은 실행되지만, 새로운 작업은 수용하지 않는다.
     **/
    void shutdown();

    /**
     * 현재 실행중인 모든 작업을 중지시키고, 대기중인 작업을 멈추고, 현재 실행되기 위해 대기중인 작업 목록을 리턴한다.
     * @return List<Callable>
     **/
    List<Runnable>  shutdownNow();

    /**
     * Excutor가 shutdown 됐는지 확인한다.
     * @return boolean
     **/
    boolean isShutdown();
    /**
     * 셧다운 실행 후 모든 작업이 종료되었는 지의 여부를 확인한다.
     * @return boolean
     **/
    boolean isTerminated();

    /**
     * shutdown 실행 후, 지정한 시간동안 모든 작업이 종료될 때까지 대기함.
     * 지정한 시간 내에 모든 작업이 종료되었는지 여부를 반환함.
     * @param timeout 초 단위
     * @return boolean
    **/
    boolean awaitTermination(long timeout) throws InterruptedException;


}
