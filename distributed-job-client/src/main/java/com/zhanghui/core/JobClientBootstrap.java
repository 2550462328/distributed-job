package com.zhanghui.core;

import com.zhanghui.core.annotation.ClientJobDetail;
import com.zhanghui.core.dto.TesseractExecutorRequest;
import com.zhanghui.core.dto.TesseractExecutorResponse;
import com.zhanghui.core.executor.ExecutorWorkerRunnable;
import com.zhanghui.core.executor.JobExecuteDelegator;
import com.zhanghui.core.scanner.HeartbeatThread;
import com.zhanghui.core.scanner.RegistryJobThread;
import com.zhanghui.feignService.IClientFeignService;
import com.zhanghui.util.threadpool.ThreadPoolFactoryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: ZhangHui
 * @date: 2020/10/22 14:38
 * @version：1.0
 */
@Slf4j
public class JobClientBootstrap {

    @Autowired
    private IClientFeignService clientFeignService;

    @Autowired(required = false)
    private List<ClientJobDetail> clientJobDetailList;

    @Value("${tesseract-admin-address}")
    private String serverAddress;

    @Value("${server.port}")
    private String serverPort;

    private RegistryJobThread registryJobThread;

    private HeartbeatThread heartbeatThread;

    private static final Set<String> CLIENT_JOB_GROUPS = ConcurrentHashMap.newKeySet();

    public void init() {
        initServiceDelegator();
        initClientJobGroups();
        if (!CollectionUtils.isEmpty(CLIENT_JOB_GROUPS)) {
            initRegistryThread();
            initHeartThread();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            log.info("程序正在关闭，执行自定义关闭策略");
            this.destroy();
        }));
    }

    private void initServiceDelegator(){
        JobServiceDelegator.adminServerAddress = serverAddress;
        JobServiceDelegator.clientFeignService = clientFeignService;
        JobServiceDelegator.clientJobDetailList = clientJobDetailList;
        JobServiceDelegator.serverPort = serverPort;
    }

    private void initClientJobGroups(){
        clientJobDetailList.parallelStream().forEach(clientJobDetail -> {
            CLIENT_JOB_GROUPS.add(clientJobDetail.getGroupName());
        });
    }

    private void initRegistryThread(){
        if(registryJobThread == null){
            registryJobThread = new RegistryJobThread();
            registryJobThread.setDaemon(true);
        }
        registryJobThread.startThread();
    }

    private void initHeartThread(){
        if(heartbeatThread == null) {
            heartbeatThread = new HeartbeatThread(CLIENT_JOB_GROUPS);
            heartbeatThread.setDaemon(true);
            heartbeatThread.setRegistryJobThread(registryJobThread);
        }
        heartbeatThread.startThread();
    }

    public void destroy() {
        registryJobThread.stopThread();
        heartbeatThread.stopThread();
        JobExecuteDelegator.stopExecute();
    }

}
