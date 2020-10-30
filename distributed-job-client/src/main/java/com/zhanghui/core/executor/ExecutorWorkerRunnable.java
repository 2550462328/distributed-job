package com.zhanghui.core.executor;

import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.dto.TesseractAdminJobNotify;
import com.zhanghui.core.dto.TesseractExecutorRequest;
import com.zhanghui.core.dto.TesseractExecutorResponse;
import com.zhanghui.core.handler.JobHandler;
import com.zhanghui.core.handler.context.JobClientContext;
import com.zhanghui.feignService.IClientFeignService;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;

import static com.zhanghui.core.constant.CommonConstant.NOTIFY_MAPPING;

/**
 * admin端调度任务的执行线程
 *
 * @author: ZhangHui
 * @date: 2020/10/27 13:59
 * @version：1.0
 */
@Slf4j
public class ExecutorWorkerRunnable implements Runnable {

    private TesseractExecutorRequest tesseractExecutorRequest;
    private IClientFeignService clientFeignService;
    private String adminServerAddress;

    public ExecutorWorkerRunnable(TesseractExecutorRequest tesseractExecutorRequest) {
        this.tesseractExecutorRequest = tesseractExecutorRequest;
        this.clientFeignService = JobServiceDelegator.clientFeignService;
        this.adminServerAddress = JobServiceDelegator.adminServerAddress;
    }

    @Override
    public void run() {
        String className = tesseractExecutorRequest.getClassName();
        TesseractAdminJobNotify tesseractAdminJobNotify = buildJobNotify(tesseractExecutorRequest);
        TesseractExecutorResponse notifyResponse = null;
        try {
            Class<?> aClass = Class.forName(className);
            JobHandler jobHandler = (JobHandler) aClass.newInstance();

            JobClientContext jobClientContext = new JobClientContext();
            jobClientContext.setShardingIndex(tesseractExecutorRequest.getShardingIndex());
            jobHandler.execute(jobClientContext);
            notifyResponse = clientFeignService.notify(new URI(adminServerAddress + NOTIFY_MAPPING), tesseractAdminJobNotify);
        } catch (Exception e) {
            log.error("执行异常:{}", e.getMessage());
            tesseractAdminJobNotify.setException(e.getMessage());
            try {
                notifyResponse = clientFeignService.notify(new URI(adminServerAddress + NOTIFY_MAPPING), tesseractAdminJobNotify);
            } catch (URISyntaxException ex) {
                log.error("执行异常URI异常:{}", e.getMessage());
            }
        }
        if (notifyResponse != null && notifyResponse.getStatus() != TesseractExecutorResponse.SUCCESS_STATUS) {
            log.error("通知执行器出错:{}", notifyResponse);
        }
        log.info("执行通知结果:{}", notifyResponse);
    }

    private TesseractAdminJobNotify buildJobNotify(TesseractExecutorRequest tesseractExecutorRequest) {
        TesseractAdminJobNotify tesseractAdminJobNotify = new TesseractAdminJobNotify();
        tesseractAdminJobNotify.setLogId(tesseractExecutorRequest.getLogId());
        tesseractAdminJobNotify.setTriggerId(tesseractExecutorRequest.getTriggerId());

        return tesseractAdminJobNotify;
    }
}
