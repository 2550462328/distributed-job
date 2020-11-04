package com.zhanghui.core.registry.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhanghui.core.JobServerBootstrap;
import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.constant.OperateResult;
import com.zhanghui.core.dto.TesseractAdminJobDetailDTO;
import com.zhanghui.core.dto.TesseractAdminRegistryFailInfo;
import com.zhanghui.core.registry.AbstractRegistryHandler;
import com.zhanghui.core.registry.RegistryTransportDTO;
import com.zhanghui.entity.TesseractGroup;
import com.zhanghui.service.ITesseractGroupService;
import lombok.extern.slf4j.Slf4j;

import static com.zhanghui.core.constant.CommonConstant.*;

/**
 * @author: ZhangHui
 * @date: 2020/11/4 11:22
 * @version：1.0
 */
@Slf4j
public class GroupRegistryHandler extends AbstractRegistryHandler {

    private final ITesseractGroupService groupService = JobServiceDelegator.groupService;

    @Override
    public OperateResult<TesseractAdminRegistryFailInfo> handler(RegistryTransportDTO registryTransportDTO) {

        TesseractAdminJobDetailDTO jobDetailDTO = registryTransportDTO.getJobDetailDTO();

        QueryWrapper<TesseractGroup> groupQueryWrapper = new QueryWrapper<>();
        groupQueryWrapper.lambda().eq(TesseractGroup::getName, jobDetailDTO.getGroupName());
        TesseractGroup tesseractGroup = groupService.getOne(groupQueryWrapper);

        if(tesseractGroup != null){
            registryTransportDTO.setTesseractGroup(tesseractGroup);
            return getSuccessor().handler(registryTransportDTO);
        }else{
            OperateResult<TesseractAdminRegistryFailInfo> operateResult = new OperateResult<>();

            tesseractGroup = new TesseractGroup();
            tesseractGroup.setName(jobDetailDTO.getGroupName());
            tesseractGroup.setCreateTime(System.currentTimeMillis());
            tesseractGroup.setCreator(DEFAULT_CREATER);
            tesseractGroup.setThreadPoolNum(DEFAULT_THREAD_NUM);
            tesseractGroup.setUpdateTime(System.currentTimeMillis());

            if (!groupService.save(tesseractGroup)) {
                String errorMessage = String.format("数据库保存失败：%s", tesseractGroup);
                log.error(errorMessage);

                TesseractAdminRegistryFailInfo registryFailInfo = TesseractAdminRegistryFailInfo.build(jobDetailDTO, SERVER_ERROR, errorMessage);
                operateResult.set(registryFailInfo);
                return operateResult;
            }

            // 为新增group开启一个SchedulerGroup
            addAndStartSchedulerGroup(tesseractGroup);

            registryTransportDTO.setTesseractGroup(tesseractGroup);
            return getSuccessor().handler(registryTransportDTO);
        }
    }


    private void addAndStartSchedulerGroup(TesseractGroup tesseractGroup) {
        JobServerBootstrap.addGroupScheduler(tesseractGroup);
    }
}
