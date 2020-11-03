package com.zhanghui.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.cache.TriggerMongoCache;
import com.zhanghui.core.cache.TriggerRedisCache;
import com.zhanghui.core.constant.OperateResult;
import com.zhanghui.core.dto.TesseractAdminJobDetailDTO;
import com.zhanghui.core.dto.TesseractAdminRegistryFailInfo;
import com.zhanghui.core.dto.TesseractAdminRegistryRequest;
import com.zhanghui.core.dto.TesseractAdminRegistryResDTO;
import com.zhanghui.core.schedule.CronExpression;
import com.zhanghui.core.JobServerBootstrap;
import com.zhanghui.entity.TesseractGroup;
import com.zhanghui.entity.TesseractJobDetail;
import com.zhanghui.entity.TesseractTrigger;
import com.zhanghui.mapper.TesseractJobDetailMapper;
import com.zhanghui.service.ITesseractGroupService;
import com.zhanghui.service.ITesseractJobDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhanghui.service.ITesseractTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import static com.zhanghui.core.constant.CommonConstant.*;

import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    @Autowired
    private ITesseractTriggerService triggerService;

    @Autowired
    private ITesseractGroupService groupService;

    @Autowired
    private TriggerMongoCache triggerMongoCache;

    private static final int PROCESSOR_RUNTIME = 5 * 1000;

    @Override
    public TesseractAdminRegistryResDTO registry(TesseractAdminRegistryRequest registryRequest) throws Exception {
        String socketAddress = registryRequest.getIp() + ":" + registryRequest.getPort();
        List<TesseractAdminJobDetailDTO> jobDetailDTOS = registryRequest.getTesseractAdminJobDetailDTOList();

        log.info("注册来自{}的Jobs[{}]", socketAddress, jobDetailDTOS);

        // 装载注册失败的Job并返回给客户端
        List<TesseractAdminRegistryFailInfo> notRegistryJobList = Collections.synchronizedList(Lists.newArrayList());

        jobDetailDTOS.stream().forEach(jobDetailDTO -> {

            try {
                QueryWrapper<TesseractTrigger> triggerQueryWrapper = new QueryWrapper<>();
                triggerQueryWrapper.lambda().eq(TesseractTrigger::getName, jobDetailDTO.getTriggerName());

                TesseractTrigger tesseractTrigger = triggerService.getOne(triggerQueryWrapper);

                if (tesseractTrigger != null) {
                    OperateResult<TesseractAdminRegistryFailInfo> operateResult = saveJobDetail(tesseractTrigger, jobDetailDTO);
                    if (!operateResult.isSuccess()) {
                        notRegistryJobList.add(operateResult.get());
                    }
                } else {
                    QueryWrapper<TesseractGroup> groupQueryWrapper = new QueryWrapper<>();
                    groupQueryWrapper.lambda().eq(TesseractGroup::getName, jobDetailDTO.getGroupName());
                    TesseractGroup tesseractGroup = groupService.getOne(groupQueryWrapper);

                    if (tesseractGroup != null) {
                        OperateResult<TesseractAdminRegistryFailInfo> operateResult = saveTriggerAndJobDetail(tesseractGroup, jobDetailDTO);
                        if (!operateResult.isSuccess()) {
                            notRegistryJobList.add(operateResult.get());
                        }
                    } else {
                        OperateResult<TesseractAdminRegistryFailInfo> operateResult = saveGroupAndTriggerAndJobDetail(jobDetailDTO);
                        if (!operateResult.isSuccess()) {
                            notRegistryJobList.add(operateResult.get());
                        }
                    }
                }
            } catch (Exception e) {
                String errorMessage = String.format("注册jobDetail出现异常:[%s]", e.getMessage());
                log.error(errorMessage, e.getCause());
                TesseractAdminRegistryFailInfo registryFailInfo = buildRegistryFailInfo(jobDetailDTO, SERVER_ERROR, errorMessage);
                notRegistryJobList.add(registryFailInfo);
            }
        });

        TesseractAdminRegistryResDTO registryResDTO = new TesseractAdminRegistryResDTO();
        registryResDTO.setNotRegistryJobList(notRegistryJobList);
        return registryResDTO;
    }

    private OperateResult<TesseractAdminRegistryFailInfo> saveJobDetail(TesseractTrigger tesseractTrigger, TesseractAdminJobDetailDTO jobDetailDTO) {
        OperateResult<TesseractAdminRegistryFailInfo> operateResult = new OperateResult<>();

        QueryWrapper<TesseractJobDetail> jobDetailQueryWrapper = new QueryWrapper<>();
        jobDetailQueryWrapper.lambda().eq(TesseractJobDetail::getClassName, jobDetailDTO.getClassName())
                .eq(TesseractJobDetail::getTriggerId, tesseractTrigger.getId());

        if (this.getOne(jobDetailQueryWrapper) != null) {
            String errorMessage = String.format("%s触发器的指定Job[%s]已存在，无需重复创建", jobDetailDTO.getTriggerName(), jobDetailDTO.getClassName());

            log.warn(errorMessage);

            TesseractAdminRegistryFailInfo registryFailInfo = buildRegistryFailInfo(jobDetailDTO, REGISTRY_REPEAT, errorMessage);
            operateResult.set(registryFailInfo);
            return operateResult;
        }

        TesseractJobDetail tesseractJobDetail = TesseractJobDetail.builder()
                .className(jobDetailDTO.getClassName())
                .triggerId(tesseractTrigger.getId())
                .createTime(System.currentTimeMillis())
                .creator(DEFAULT_CREATER).build();

        if (this.save(tesseractJobDetail)) {
            return operateResult;
        }

        String errorMessage = String.format("数据库保存失败：%s", tesseractJobDetail);
        log.error(errorMessage);

        TesseractAdminRegistryFailInfo registryFailInfo = buildRegistryFailInfo(jobDetailDTO, SERVER_ERROR, errorMessage);
        operateResult.set(registryFailInfo);
        return operateResult;

    }

    private OperateResult saveTriggerAndJobDetail(TesseractGroup tesseractGroup, TesseractAdminJobDetailDTO jobDetailDTO) {
        OperateResult<TesseractAdminRegistryFailInfo> operateResult = new OperateResult<>();

        if (!isValidCronExpression(jobDetailDTO.getCron())) {
            String errorMessage = String.format("注册trigger %s 的cron表达式 %S 无效", jobDetailDTO.getTriggerName(), jobDetailDTO.getCron());

            log.warn(errorMessage);

            TesseractAdminRegistryFailInfo registryFailInfo = buildRegistryFailInfo(jobDetailDTO, REGISTRY_INFO_ERROR, errorMessage);
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

        TesseractTrigger tesseractTrigger = buildTesseractTrigger(tesseractGroup, cronExpression, jobDetailDTO);

        // 保存Trigger到数据库
        if (!triggerService.save(tesseractTrigger)) {

            String errorMessage = String.format("数据库保存失败：%s", tesseractTrigger);
            log.error(errorMessage);

            TesseractAdminRegistryFailInfo registryFailInfo = buildRegistryFailInfo(jobDetailDTO, SERVER_ERROR, errorMessage);
            operateResult.set(registryFailInfo);
            return operateResult;
        }

        //保存Trigger到mongodb缓存
        triggerMongoCache.addTriggerToCache(tesseractTrigger);
        return saveJobDetail(tesseractTrigger, jobDetailDTO);
    }

    private OperateResult saveGroupAndTriggerAndJobDetail(TesseractAdminJobDetailDTO jobDetailDTO) {
        OperateResult<TesseractAdminRegistryFailInfo> operateResult = new OperateResult<>();

        TesseractGroup tesseractGroup = new TesseractGroup();
        tesseractGroup.setName(jobDetailDTO.getGroupName());
        tesseractGroup.setCreateTime(System.currentTimeMillis());
        tesseractGroup.setCreator(DEFAULT_CREATER);
        tesseractGroup.setThreadPoolNum(DEFAULT_THREAD_NUM);
        tesseractGroup.setUpdateTime(System.currentTimeMillis());

        if (!groupService.save(tesseractGroup)) {

            String errorMessage = String.format("数据库保存失败：%s", tesseractGroup);
            log.error(errorMessage);

            TesseractAdminRegistryFailInfo registryFailInfo = buildRegistryFailInfo(jobDetailDTO, SERVER_ERROR, errorMessage);
            operateResult.set(registryFailInfo);
            return operateResult;
        }

        // 为新增group开启一个SchedulerGroup
        addAndStartSchedulerGroup(tesseractGroup);

        return saveTriggerAndJobDetail(tesseractGroup, jobDetailDTO);

    }

    private boolean isValidCronExpression(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

    private TesseractAdminRegistryFailInfo buildRegistryFailInfo(TesseractAdminJobDetailDTO jobDetailDTO, int errorCode, String errorMessage) {
        TesseractAdminRegistryFailInfo registryFailInfo = new TesseractAdminRegistryFailInfo();
        registryFailInfo.setJobDetailDTO(jobDetailDTO);
        registryFailInfo.setErrorCode(errorCode);
        registryFailInfo.setErrorMessage(errorMessage);
        return registryFailInfo;
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
        tesseractTrigger.setGroupName(tesseractGroup.getName());
        tesseractTrigger.setName(jobDetailDTO.getTriggerName());
        tesseractTrigger.setRetryCount(jobDetailDTO.getRetryTimes());
        tesseractTrigger.setShardingNum(jobDetailDTO.getShardingNum());
        tesseractTrigger.setStatus(jobDetailDTO.getStatus());
        tesseractTrigger.setStrategy(jobDetailDTO.getStrategy());
        tesseractTrigger.setDescription(jobDetailDTO.getDesc());
        tesseractTrigger.setLogFlag(jobDetailDTO.getIsLog());

        return tesseractTrigger;
    }

    private void addAndStartSchedulerGroup(TesseractGroup tesseractGroup) {
        JobServerBootstrap.addGroupScheduler(tesseractGroup);
    }
}
