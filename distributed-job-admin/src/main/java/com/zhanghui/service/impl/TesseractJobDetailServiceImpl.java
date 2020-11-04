package com.zhanghui.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.zhanghui.core.constant.OperateResult;
import com.zhanghui.core.dto.TesseractAdminJobDetailDTO;
import com.zhanghui.core.dto.TesseractAdminRegistryFailInfo;
import com.zhanghui.core.dto.TesseractAdminRegistryRequest;
import com.zhanghui.core.dto.TesseractAdminRegistryResDTO;
import com.zhanghui.core.registry.AbstractRegistryHandler;
import com.zhanghui.core.registry.RegistryTransportDTO;
import com.zhanghui.core.registry.impl.GroupRegistryHandler;
import com.zhanghui.core.registry.impl.JobDetailRegistryHandler;
import com.zhanghui.core.registry.impl.TriggerRegistryHandler;
import com.zhanghui.entity.TesseractJobDetail;
import com.zhanghui.mapper.TesseractJobDetailMapper;
import com.zhanghui.service.ITesseractGroupService;
import com.zhanghui.service.ITesseractJobDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static com.zhanghui.core.constant.CommonConstant.SERVER_ERROR;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
@Service
@Slf4j
public class TesseractJobDetailServiceImpl extends ServiceImpl<TesseractJobDetailMapper, TesseractJobDetail> implements ITesseractJobDetailService {

    @Override
    public TesseractAdminRegistryResDTO registry(TesseractAdminRegistryRequest registryRequest){
        String socketAddress = registryRequest.getIp() + ":" + registryRequest.getPort();
        List<TesseractAdminJobDetailDTO> jobDetailDTOS = registryRequest.getTesseractAdminJobDetailDTOList();

        log.info("注册来自{}的Jobs[{}]", socketAddress, jobDetailDTOS);

        // 装载注册失败的Job并返回给客户端
        List<TesseractAdminRegistryFailInfo> notRegistryJobList = Collections.synchronizedList(Lists.newArrayList());

        jobDetailDTOS.stream().forEach(jobDetailDTO -> {

            RegistryTransportDTO registryTransportDTO = new RegistryTransportDTO();
            registryTransportDTO.setJobDetailDTO(jobDetailDTO);

            try {
                // 注册
                doRegistry(notRegistryJobList, registryTransportDTO);

            } catch (Exception e) {
                String errorMessage = String.format("注册jobDetail出现异常:[%s]", e.getMessage());
                log.error(errorMessage, e.getCause());
                TesseractAdminRegistryFailInfo registryFailInfo = TesseractAdminRegistryFailInfo.build(jobDetailDTO, SERVER_ERROR, errorMessage);
                notRegistryJobList.add(registryFailInfo);
            }
        });

        return  new TesseractAdminRegistryResDTO(notRegistryJobList);
    }

    private void doRegistry(List<TesseractAdminRegistryFailInfo> notRegistryJobList , RegistryTransportDTO registryTransportDTO){
        AbstractRegistryHandler groupRegistryHandler = new GroupRegistryHandler();
        AbstractRegistryHandler triggerRegistryHandler = new TriggerRegistryHandler();
        AbstractRegistryHandler jobDetailRegistryHandler = new JobDetailRegistryHandler();

        groupRegistryHandler.setSuccessor(triggerRegistryHandler);
        triggerRegistryHandler.setSuccessor(jobDetailRegistryHandler);

        OperateResult<TesseractAdminRegistryFailInfo> operateResult = groupRegistryHandler.handler(registryTransportDTO);

        if(!operateResult.isSuccess()){
            notRegistryJobList.add(operateResult.get());
        }
    }

}
