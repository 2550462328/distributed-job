package com.zhanghui.core.registry.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.constant.OperateResult;
import com.zhanghui.core.dto.TesseractAdminJobDetailDTO;
import com.zhanghui.core.dto.TesseractAdminRegistryFailInfo;
import com.zhanghui.core.registry.AbstractRegistryHandler;
import com.zhanghui.core.registry.RegistryTransportDTO;
import com.zhanghui.entity.TesseractJobDetail;
import com.zhanghui.entity.TesseractTrigger;
import com.zhanghui.service.ITesseractJobDetailService;
import lombok.extern.slf4j.Slf4j;

import static com.zhanghui.core.constant.CommonConstant.*;

/**
 * @author: ZhangHui
 * @date: 2020/11/4 13:51
 * @version：1.0
 */
@Slf4j
public class JobDetailRegistryHandler  extends AbstractRegistryHandler {

    private final ITesseractJobDetailService jobDetailService = JobServiceDelegator.jobDetailService;

    @Override
    public OperateResult<TesseractAdminRegistryFailInfo> handler(RegistryTransportDTO registryTransportDTO) {

        OperateResult<TesseractAdminRegistryFailInfo> operateResult = new OperateResult<>();

        TesseractAdminJobDetailDTO jobDetailDTO = registryTransportDTO.getJobDetailDTO();

        TesseractTrigger tesseractTrigger = registryTransportDTO.getTesseractTrigger();

        QueryWrapper<TesseractJobDetail> jobDetailQueryWrapper = new QueryWrapper<>();
        jobDetailQueryWrapper.lambda().eq(TesseractJobDetail::getClassName, jobDetailDTO.getClassName())
                .eq(TesseractJobDetail::getTriggerId, tesseractTrigger.getId());

        if (jobDetailService.getOne(jobDetailQueryWrapper) != null) {
            String errorMessage = String.format("%s触发器的指定Job[%s]已存在，无需重复创建", jobDetailDTO.getTriggerName(), jobDetailDTO.getClassName());
            log.warn(errorMessage);
            TesseractAdminRegistryFailInfo registryFailInfo = TesseractAdminRegistryFailInfo.build(jobDetailDTO, REGISTRY_REPEAT, errorMessage);
            operateResult.set(registryFailInfo);
            return operateResult;
        }

        TesseractJobDetail tesseractJobDetail = TesseractJobDetail.builder()
                .className(jobDetailDTO.getClassName())
                .triggerId(tesseractTrigger.getId())
                .createTime(System.currentTimeMillis())
                .creator(DEFAULT_CREATER).build();

        if (jobDetailService.save(tesseractJobDetail)) {
            return operateResult;
        }

        String errorMessage = String.format("数据库保存失败：%s", tesseractJobDetail);
        log.error(errorMessage);

        TesseractAdminRegistryFailInfo registryFailInfo = TesseractAdminRegistryFailInfo.build(jobDetailDTO, SERVER_ERROR, errorMessage);
        operateResult.set(registryFailInfo);
        return operateResult;
    }
}
