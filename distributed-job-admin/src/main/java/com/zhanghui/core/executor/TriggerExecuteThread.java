package com.zhanghui.core.executor;

import com.alibaba.fastjson.JSON;
import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.bean.TaskContextInfo;
import com.zhanghui.core.bean.TaskInfo;
import com.zhanghui.core.dto.TesseractExecutorRequest;
import com.zhanghui.core.dto.TesseractExecutorResponse;
import com.zhanghui.core.executor.TaskExecuteDelegator;
import com.zhanghui.entity.*;
import com.zhanghui.service.ITesseractLogService;
import feignService.IAdminFeignService;
import com.zhanghui.service.ITesseractFiredTriggerService;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.util.Objects;

import static com.zhanghui.core.constant.CommonConstant.*;
import static com.zhanghui.constant.AdminConstant.*;

/**
 * @author: ZhangHui
 * @date: 2020/10/21 16:50
 * @version：1.0
 */
@Slf4j
public class TriggerExecuteThread implements Runnable {

    private final ITesseractFiredTriggerService firedTriggerService;

    private final TesseractTrigger tesseractTrigger;

    private final TesseractJobDetail tesseractJobDetail;

    private final IAdminFeignService feignService;

    private final ITesseractLogService tesseractLogService;

    private final boolean isLog;

    private final TaskContextInfo taskContextInfo;

    private TesseractExecutorDetail executorDetail;

    public TriggerExecuteThread(TaskContextInfo taskContextInfo, TaskInfo taskInfo) {
        this.tesseractTrigger = taskInfo.getTrigger();
        this.tesseractJobDetail = taskInfo.getJobDetail();
        this.executorDetail = taskInfo.getExecutorDetail();

        this.firedTriggerService = JobServiceDelegator.firedTriggerService;
        this.feignService = JobServiceDelegator.feignService;
        this.tesseractLogService = JobServiceDelegator.logService;
        this.isLog = tesseractTrigger.getLogFlag();
        this.taskContextInfo = taskContextInfo;
    }

    @Override
    public void run() {
        // 构建请求
        TesseractExecutorRequest executorRequest = createExecutorRequest();

        // 先写入日志
        TesseractLog tesseractLog = createExecutorRequestLog(executorRequest);

        // 保存触发器执行情况
        TesseractFiredTrigger tesseractFiredTrigger = saveFiredTrigger();

        // 是否进行日志追踪
        if (isLog) {
            executorRequest.setLogId(tesseractLog.getId());
        }

        //发送调度请求
        TesseractExecutorResponse response = executeRequestURI(executorRequest, tesseractLog);

        log.info("调度返回结果:{}", response.getBody());

        //移出执行表
        firedTriggerService.removeById(tesseractFiredTrigger.getId());
    }

    private TesseractFiredTrigger saveFiredTrigger() {

        TesseractFiredTrigger tesseractFiredTrigger = new TesseractFiredTrigger();
        tesseractFiredTrigger.setCreateTime(System.currentTimeMillis());
        tesseractFiredTrigger.setName(tesseractTrigger.getName());
        tesseractFiredTrigger.setTriggerId(tesseractTrigger.getId());
        tesseractFiredTrigger.setClassName(tesseractJobDetail.getClassName());
        tesseractFiredTrigger.setExecutorDetailId(executorDetail.getId());
        tesseractFiredTrigger.setSocket(executorDetail.getSocket());
        firedTriggerService.save(tesseractFiredTrigger);

        return tesseractFiredTrigger;
    }

    private TesseractLog createExecutorRequestLog(TesseractExecutorRequest tesseractExecutorRequest) {

        if (!isLog) {
            return null;
        }
        TesseractLog tesseractLog = new TesseractLog();
        tesseractLog.setClassName(tesseractJobDetail.getClassName());
        tesseractLog.setCreateTime(System.currentTimeMillis());
        tesseractLog.setCreator(DEFAULT_CREATER);
        tesseractLog.setGroupId(tesseractTrigger.getGroupId());
        tesseractLog.setGroupName(tesseractTrigger.getGroupName());
        tesseractLog.setSocket(executorDetail.getSocket());
        tesseractLog.setStatus(LOG_WAIT);
        tesseractLog.setTriggerName(tesseractTrigger.getName());
        tesseractLog.setEndTime(0L);

        tesseractLog.setMsg(JSON.toJSONString(tesseractExecutorRequest));

        tesseractLogService.save(tesseractLog);

        return tesseractLog;
    }

    private TesseractExecutorRequest createExecutorRequest() {
        TesseractExecutorRequest executorRequest = new TesseractExecutorRequest();
        executorRequest.setClassName(tesseractJobDetail.getClassName());
        executorRequest.setShardingIndex(tesseractTrigger.getShardingNum());
        executorRequest.setTriggerId(tesseractTrigger.getId());

        return executorRequest;
    }

    /**
     * 执行feign的远程调用
     */
    private TesseractExecutorResponse executeRequestURI(TesseractExecutorRequest executorRequest, TesseractLog tesseractLog) {
        TesseractExecutorResponse response;

        log.info("开始调度:{}", executorRequest);

        try {
            response = feignService.sendToExecutor(new URI(HTTP_PREFIX + executorDetail.getSocket() + EXECUTE_MAPPING), executorRequest);
            if (response.getStatus() != TesseractExecutorResponse.SUCCESS_STATUS && isRetryAble()) {
                response = retryRequest(executorRequest);
            }
        } catch (Exception e) {
            log.error("出现调度异常:[{}]", e.getMessage(), e.getCause());
            response = TesseractExecutorResponse.builder().body(e.getMessage()).status(TesseractExecutorResponse.FAIL_STAUTS).build();

            // 是否重试
            if (isRetryAble()) {
                response = retryRequest(executorRequest);
            }
        }

        if(isLog){
            if(response.getStatus() == TesseractExecutorResponse.SUCCESS_STATUS){
                tesseractLog.setStatus(LOG_NO_CONFIRM);
            }else{
                tesseractLog.setStatus(LOG_FAIL);
                tesseractLog.setMsg(JSON.toJSONString(response.getBody()));
            }
            tesseractLog.setEndTime(System.currentTimeMillis());
            tesseractLogService.updateById(tesseractLog);
        }
        return response;
    }

    /**
     * 重试条件
     */
    private boolean isRetryAble() {
        return tesseractTrigger.getRetryCount() > 0
                && !Objects.equals(tesseractTrigger.getStrategy(), SCHEDULER_STRATEGY_BROADCAST)
                && !Objects.equals(tesseractTrigger.getStrategy(), SCHEDULER_STRATEGY_SHARDING);
    }

    private TesseractExecutorResponse retryRequest(TesseractExecutorRequest executorRequest) {
        TesseractExecutorResponse response = null;
        for (int i = 0; i < tesseractTrigger.getRetryCount(); i++) {
            // 每次尽量获取不同的executorDetail去执行jobDetail
            executorDetail = TaskExecuteDelegator.routeExecutorDetail(taskContextInfo,executorDetail);

            try {
                response = feignService.sendToExecutor(new URI(HTTP_PREFIX + executorDetail.getSocket() + EXECUTE_MAPPING), executorRequest);
                if (response.getStatus() == TesseractExecutorResponse.SUCCESS_STATUS) {
                    return response;
                }
            } catch (Exception e) {
                log.error("出现调度异常:[{}]", e.getMessage(), e.getCause());
                response = TesseractExecutorResponse.builder().body(e.getMessage()).status(TesseractExecutorResponse.FAIL_STAUTS).build();
            }
        }
        return response;
    }


}
