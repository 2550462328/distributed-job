package com.zhanghui.core.registry.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.cache.TriggerMongoCache;
import com.zhanghui.core.constant.OperateResult;
import com.zhanghui.core.dto.TesseractAdminJobDetailDTO;
import com.zhanghui.core.dto.TesseractAdminRegistryFailInfo;
import com.zhanghui.core.registry.AbstractRegistryHandler;
import com.zhanghui.core.registry.RegistryTransportDTO;
import com.zhanghui.core.schedule.CronExpression;
import com.zhanghui.entity.TesseractGroup;
import com.zhanghui.entity.TesseractTrigger;
import com.zhanghui.service.ITesseractTriggerService;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.util.Date;

import static com.zhanghui.core.constant.CommonConstant.*;

/**
 * @author: ZhangHui
 * @date: 2020/11/4 12:43
 * @version：1.0
 */
@Slf4j
public class TriggerRegistryHandler extends AbstractRegistryHandler {

    private final ITesseractTriggerService triggerService = JobServiceDelegator.triggerService;

    private static final int PROCESSOR_RUNTIME = 5 * 1000;

    private final TriggerMongoCache triggerMongoCache = JobServiceDelegator.triggerMongoCache;

    @Override
    public OperateResult<TesseractAdminRegistryFailInfo> handler(RegistryTransportDTO registryTransportDTO) {
        TesseractAdminJobDetailDTO jobDetailDTO = registryTransportDTO.getJobDetailDTO();

        QueryWrapper<TesseractTrigger> triggerQueryWrapper = new QueryWrapper<>();
        triggerQueryWrapper.lambda().eq(TesseractTrigger::getName, jobDetailDTO.getTriggerName());

        TesseractTrigger tesseractTrigger = triggerService.getOne(triggerQueryWrapper);

        if (tesseractTrigger != null) {

            registryTransportDTO.setTesseractTrigger(tesseractTrigger);
            return getSuccessor().handler(registryTransportDTO);
        }else{
            OperateResult<TesseractAdminRegistryFailInfo> operateResult = new OperateResult<>();

            if (!isValidCronExpression(jobDetailDTO.getCron())) {
                String errorMessage = String.format("注册trigger %s 的cron表达式 %S 无效", jobDetailDTO.getTriggerName(), jobDetailDTO.getCron());
                log.warn(errorMessage);

                TesseractAdminRegistryFailInfo registryFailInfo = TesseractAdminRegistryFailInfo.build(jobDetailDTO, REGISTRY_INFO_ERROR, errorMessage);
                operateResult.set(registryFailInfo);
                return operateResult;
            }

            //新建trigger
            CronExpression cronExpression = null;
            try {
                cronExpression = new CronExpression(jobDetailDTO.getCron());
            } catch (ParseException e) {
                //ignore
            }

            TesseractGroup tesseractGroup = registryTransportDTO.getTesseractGroup();

            tesseractTrigger = buildTesseractTrigger(tesseractGroup, cronExpression, jobDetailDTO);

            // 保存Trigger到数据库
            if (!triggerService.save(tesseractTrigger)) {
                String errorMessage = String.format("数据库保存失败：%s", tesseractTrigger);
                log.error(errorMessage);

                TesseractAdminRegistryFailInfo registryFailInfo = TesseractAdminRegistryFailInfo.build(jobDetailDTO, SERVER_ERROR, errorMessage);
                operateResult.set(registryFailInfo);
                return operateResult;
            }

            //保存Trigger到mongodb缓存
            triggerMongoCache.addTriggerToCache(tesseractTrigger);

            registryTransportDTO.setTesseractTrigger(tesseractTrigger);
            return getSuccessor().handler(registryTransportDTO);
        }
    }

    private boolean isValidCronExpression(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    private TesseractTrigger buildTesseractTrigger(TesseractGroup tesseractGroup, CronExpression cronExpression, TesseractAdminJobDetailDTO jobDetailDTO) {
        TesseractTrigger tesseractTrigger = new TesseractTrigger();
        tesseractTrigger.setPrevTriggerTime(0L);
        tesseractTrigger.setNextTriggerTime(cronExpression.getTimeAfter(new Date()).getTime() + PROCESSOR_RUNTIME);
        tesseractTrigger.setCreateTime(System.currentTimeMillis());
        tesseractTrigger.setUpdateTime(System.currentTimeMillis());
        tesseractTrigger.setCreator(DEFAULT_CREATER);
        tesseractTrigger.setCron(jobDetailDTO.getCron());
        tesseractTrigger.setGroupId(tesseractGroup.getId());
        tesseractTrigger.setGroupName(jobDetailDTO.getGroupName());
        tesseractTrigger.setName(jobDetailDTO.getTriggerName());
        tesseractTrigger.setRetryCount(jobDetailDTO.getRetryTimes());
        tesseractTrigger.setShardingNum(jobDetailDTO.getShardingNum());
        tesseractTrigger.setStatus(jobDetailDTO.getStatus());
        tesseractTrigger.setStrategy(jobDetailDTO.getStrategy());
        tesseractTrigger.setDescription(jobDetailDTO.getDesc());
        tesseractTrigger.setLogFlag(jobDetailDTO.getIsLog());

        return tesseractTrigger;
    }
}
