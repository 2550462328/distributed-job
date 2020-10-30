package com.zhanghui.core.scanner;

import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.dto.TesseractExecutorResponse;
import com.zhanghui.core.dto.TesseractHeartbeatRequest;
import com.zhanghui.core.executor.JobExecuteDelegator;
import com.zhanghui.core.executor.ThreadPoolInfo;
import com.zhanghui.core.lifestyle.IThreadLifycycle;
import com.zhanghui.feignService.IClientFeignService;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Set;

import static com.zhanghui.core.constant.CommonConstant.*;

/**
 * @author: ZhangHui
 * @date: 2020/10/22 14:55
 * @version：1.0
 */
@Slf4j
public class HeartbeatThread extends Thread implements IThreadLifycycle {

    private volatile boolean isStop = false;
    private static final Integer HEART_INTERVAL_TIME = 10 * 1000;

    private RegistryJobThread registryJobThread;

    private final IClientFeignService clientFeignService;
    private final String adminServerAddress;
    private final String serverPort;

    private final Set<String> clientJobGroups;

    public HeartbeatThread(Set<String> clientJobGroups) {
        this.clientFeignService = JobServiceDelegator.clientFeignService;
        this.adminServerAddress = JobServiceDelegator.adminServerAddress;
        this.serverPort = JobServiceDelegator.serverPort;
        this.clientJobGroups = clientJobGroups;
    }

    public void setRegistryJobThread(RegistryJobThread registryJobThread) {
        this.registryJobThread = registryJobThread;
    }

    @Override
    public void initThread() {

    }

    @Override
    public void startThread() {
        this.start();
    }

    @Override
    public void run() {
        log.info("HeartbeatThread start");
        while (!isStop) {
            //开始心跳
            //todo 在集群情况下会向每一台admin发送心跳
            heartbeat();
            try {
                Thread.sleep(HEART_INTERVAL_TIME);
            } catch (InterruptedException e) {
            }
        }
    }

    private void heartbeat() {
        try {
            TesseractHeartbeatRequest tesseractHeartbeatRequest = buildHeartbeatRequest();

            TesseractExecutorResponse response = clientFeignService.heartbeat(new URI(adminServerAddress + HEARTBEAT_MAPPING), tesseractHeartbeatRequest);

            if (response.getStatus() == TesseractExecutorResponse.SUCCESS_STATUS) {
                log.info("心跳成功");
                return;
            }
            if (response.getStatus() == EXECUTOR_DETAIL_NOT_FIND) {
                log.info("机器{}已失效，将重新发起注册", adminServerAddress);
                // 重新发起注册
                registryJobThread.startRegistry();
                return;
            }
            log.error("心跳失败:{}", response);
        } catch (Exception e) {
            log.error("心跳失败:{}", e.getMessage(),e.getCause());
        }
    }

    public TesseractHeartbeatRequest buildHeartbeatRequest() throws UnknownHostException {
        TesseractHeartbeatRequest tesseractHeartbeatRequest = new TesseractHeartbeatRequest();

        ThreadPoolInfo threadPoolInfo = JobExecuteDelegator.getThreadPoolInfo();
        int activeCount = threadPoolInfo.getActiveCount();
        int maximumPoolSize = threadPoolInfo.getMaximumPoolSize();

        tesseractHeartbeatRequest.setActiveCount(activeCount);
        tesseractHeartbeatRequest.setMaximumPoolSize(maximumPoolSize);
        tesseractHeartbeatRequest.setSocket(InetAddress.getLocalHost().getHostAddress() + ":" + serverPort);
        tesseractHeartbeatRequest.setClientJobGroups(clientJobGroups);

        return tesseractHeartbeatRequest;
    }

    @Override
    public void stopThread() {
        isStop = true;
        this.interrupt();
    }
}
