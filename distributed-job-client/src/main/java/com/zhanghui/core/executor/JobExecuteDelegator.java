package com.zhanghui.core.executor;

import com.zhanghui.core.dto.TesseractExecutorRequest;
import com.zhanghui.core.dto.TesseractExecutorResponse;
import com.zhanghui.util.threadpool.ThreadPoolFactoryUtils;

import java.lang.management.ThreadInfo;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: ZhangHui
 * @date: 2020/10/27 14:28
 * @version：1.0
 */
public class JobExecuteDelegator {


    private static final String THREADPOOL_NAME_PREFIX = "client-job-notify-";

    private static final ExecutorService jobNotifyHandlerThreadPool = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent(THREADPOOL_NAME_PREFIX);

    public static ThreadPoolInfo getThreadPoolInfo(){
        ThreadPoolInfo threadPoolInfoInfo = new ThreadPoolInfo();
        threadPoolInfoInfo.setActiveCount(((ThreadPoolExecutor)jobNotifyHandlerThreadPool).getActiveCount());
        threadPoolInfoInfo.setMaximumPoolSize(((ThreadPoolExecutor)jobNotifyHandlerThreadPool).getMaximumPoolSize());
        return threadPoolInfoInfo;
    }

    /**
     * 开始执行任务，扔到线程池后发送成功执行通知，执行完毕后发送异步执行成功通知
     *
     * @param tesseractExecutorRequest
     * @return
     */
    public static TesseractExecutorResponse execute(TesseractExecutorRequest tesseractExecutorRequest) {
        jobNotifyHandlerThreadPool.execute(new ExecutorWorkerRunnable(tesseractExecutorRequest));
        return TesseractExecutorResponse.builder().status(TesseractExecutorResponse.SUCCESS_STATUS).body("成功进入队列").build();
    }

    public static void stopExecute(){
        jobNotifyHandlerThreadPool.shutdown();
    }
}
