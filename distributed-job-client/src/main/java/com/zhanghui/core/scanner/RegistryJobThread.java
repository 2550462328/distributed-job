package com.zhanghui.core.scanner;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.zhanghui.core.JobClientBootstrap;
import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.annotation.ClientJobDetail;
import com.zhanghui.core.dto.*;
import com.zhanghui.core.lifestyle.IThreadLifycycle;
import com.zhanghui.exception.TesseractException;
import com.zhanghui.feignService.IClientFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.*;

import static com.zhanghui.core.constant.CommonConstant.REGISTRY_MAPPING;
import static com.zhanghui.core.constant.CommonConstant.SERVER_ERROR;

/**
 * @author: ZhangHui
 * @date: 2020/10/22 14:55
 * @version：1.0
 */
@Slf4j
public class RegistryJobThread extends Thread implements IThreadLifycycle {

    private volatile boolean isStop = false;

    private static final int REGISTRY_INTEVAL_TIME = 3 * 1000;

    // todo admin端所有的配置都可以由用户指定

    private static final int REPEAT_REGISTRY_TIMES = 2 * 1000;

    private final IClientFeignService clientFeignService = JobServiceDelegator.clientFeignService;
    private final String adminServerAddress = JobServiceDelegator.adminServerAddress;
    private final String serverPort = JobServiceDelegator.serverPort;
    private final List<ClientJobDetail> clientJobDetailList = JobServiceDelegator.clientJobDetailList;

    public RegistryJobThread() {
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
        log.info("客户端注册线程启动....^0^");
        try {
            List<TesseractAdminJobDetailDTO> jobDetailDTOList = initRegistry();
            TesseractAdminRegistryRequest tesseractAdminRegistryRequest = buildRequest(jobDetailDTOList);

            while (!isStop) {
                //todo serverAddress可以是集群，集群注册在这里处理
                TesseractExecutorResponse response = doRegistry(tesseractAdminRegistryRequest);

                afterRegistry(response.getBody());

                Thread.sleep(REGISTRY_INTEVAL_TIME);
            }
        } catch (Exception e) {
            log.error("注册[{}]出现异常：{}", clientJobDetailList, e.getMessage(), e.getCause());
        }
    }

    private TesseractExecutorResponse doRegistry(TesseractAdminRegistryRequest tesseractAdminRegistryRequest) throws URISyntaxException {
        log.info("开始注册，注册内容:{}", tesseractAdminRegistryRequest);

        TesseractExecutorResponse response = clientFeignService.registry(new URI(adminServerAddress + REGISTRY_MAPPING), tesseractAdminRegistryRequest);
        if (response.getStatus() == TesseractExecutorResponse.SUCCESS_STATUS) {
            log.info("注册成功,当前未成功注册的JobDetail信息有:{}", response.getBody());
            return response;
        }
        log.error("注册失败,状态码：{},信息：{}", response.getStatus(), response.getBody());
        return response;
    }

    private void afterRegistry(Object respBody) throws UnknownHostException, URISyntaxException {

        if (respBody == null) {
            return;
        }

        TesseractAdminRegistryResDTO registryResDTO = JSON.parseObject(JSON.toJSONString(respBody), TesseractAdminRegistryResDTO.class);

        List<TesseractAdminRegistryFailInfo> registryFailInfoList = registryResDTO.getNotRegistryJobList();

        if (CollectionUtils.isEmpty(registryFailInfoList)) {
            isStop = true;
            return;
        }

        List<TesseractAdminJobDetailDTO> repeatRegistryJobList = Collections.synchronizedList(Lists.newArrayListWithCapacity(registryFailInfoList.size()));

        registryFailInfoList.stream().forEach(registryFailInfo -> {

            // 处理需要重复注册的JobDetails
            if (Objects.equals(SERVER_ERROR, registryFailInfo.getErrorCode())) {
                repeatRegistryJobList.add(registryFailInfo.getJobDetailDTO());
            }

            // 其他错误直接在客户端启动的时候传递给他们了
        });

        if (CollectionUtils.isEmpty(repeatRegistryJobList)) {
            isStop = true;
            return;
        }

        // 尝试重新注册
        repeatRegistry(repeatRegistryJobList);
    }

    private void repeatRegistry(List<TesseractAdminJobDetailDTO> repeatRegistryJobList) throws UnknownHostException, URISyntaxException {
        TesseractAdminRegistryRequest tesseractAdminRegistryRequest = buildRequest(repeatRegistryJobList);

        TesseractExecutorResponse response;

        for (int i = 0; i < REPEAT_REGISTRY_TIMES; i++) {
            response = doRegistry(tesseractAdminRegistryRequest);
            if (CollectionUtils.isEmpty(((TesseractAdminRegistryResDTO) response.getBody()).getNotRegistryJobList())) {
                isStop = true;
                return;
            }
        }

        isStop = true;
    }

    private TesseractAdminRegistryRequest buildRequest(List<TesseractAdminJobDetailDTO> jobDetailDTOList) throws UnknownHostException {

        TesseractAdminRegistryRequest tesseractAdminRegistryRequest = new TesseractAdminRegistryRequest();
        tesseractAdminRegistryRequest.setIp(InetAddress.getLocalHost().getHostAddress());
        tesseractAdminRegistryRequest.setPort(serverPort);

        tesseractAdminRegistryRequest.setTesseractAdminJobDetailDTOList(jobDetailDTOList);
        return tesseractAdminRegistryRequest;
    }

    private List<TesseractAdminJobDetailDTO> initRegistry() {

        List<TesseractAdminJobDetailDTO> jobDetailDTOList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(clientJobDetailList)) {
            clientJobDetailList.stream().forEach(clientJobDetail -> {
                TesseractAdminJobDetailDTO tesseractAdminJobDetailDTO = new TesseractAdminJobDetailDTO();
                tesseractAdminJobDetailDTO.setClassName(clientJobDetail.getClassName());
                tesseractAdminJobDetailDTO.setTriggerName(clientJobDetail.getTriggerName());
                tesseractAdminJobDetailDTO.setGroupName(clientJobDetail.getGroupName());
                tesseractAdminJobDetailDTO.setCron(clientJobDetail.getCron());

                tesseractAdminJobDetailDTO.setShardingNum(clientJobDetail.getShardingNum());
                tesseractAdminJobDetailDTO.setStrategy(clientJobDetail.getStrategy());
                tesseractAdminJobDetailDTO.setDesc(clientJobDetail.getDesc());
                tesseractAdminJobDetailDTO.setIsLog(clientJobDetail.getIsLog());
                tesseractAdminJobDetailDTO.setRetryTimes(clientJobDetail.getRetryTimes());
                tesseractAdminJobDetailDTO.setStatus(clientJobDetail.getStatus());

                jobDetailDTOList.add(tesseractAdminJobDetailDTO);
            });
        }
        return jobDetailDTOList;
    }

    @Override
    public void stopThread() {
        isStop = true;
        this.interrupt();
    }

    /**
     * 将它从睡眠中唤醒注册
     */
    public void startRegistry() {
        isStop = false;
        this.start();
    }
}
