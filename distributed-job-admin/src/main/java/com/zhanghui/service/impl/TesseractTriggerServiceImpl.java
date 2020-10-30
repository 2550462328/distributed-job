package com.zhanghui.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.cache.TriggerRedisCache;
import com.zhanghui.core.schedule.CronExpression;
import com.zhanghui.entity.TesseractTrigger;
import com.zhanghui.mapper.TesseractTriggerMapper;
import com.zhanghui.service.ITesseractLockService;
import com.zhanghui.service.ITesseractTriggerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import static com.zhanghui.constant.AdminConstant.*;

import java.text.ParseException;
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
public class TesseractTriggerServiceImpl extends ServiceImpl<TesseractTriggerMapper, TesseractTrigger> implements ITesseractTriggerService {

    @Autowired
    private ITesseractLockService lockService;

    private final TriggerRedisCache redisCache = JobServiceDelegator.triggerRedisCache;

    /**
     * 批量获取next_trigger_time在指定时间之后的触发器
     *
     * @param groupName      条件
     * @param time           条件 指定时间
     * @param timeWindowSize 考虑程序运行时间，所以需要加上一个timeWindowSize缓冲时间，因此，next_trigger_time必须大于time + timeWindowSize
     * @return java.util.List<com.zhanghui.entity.TesseractTrigger>
     * @author ZhangHui
     * @date 2020/10/21
     */
    @Transactional
    @Override
    @Deprecated
    public List<TesseractTrigger> findTriggerWithLock(String groupName, int triggerSize, long time, Integer timeWindowSize) {
        // 一个trigger事件只能触发一次，避免分布式环境下多个服务端同时拿到同一个trigger
        lockService.lock(TRIGGER_LOCK_NAME, groupName);

        QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();

        Page page = new Page(1, triggerSize);

        queryWrapper.lambda()
                .eq(TesseractTrigger::getGroupName, groupName)
                .le(TesseractTrigger::getNextTriggerTime, time + timeWindowSize)
                .eq(TesseractTrigger::getStatus, TRGGER_STATUS_STARTING)
                .orderByDesc(TesseractTrigger::getNextTriggerTime);

        IPage<TesseractTrigger> triggersPage = page(page, queryWrapper);

        List<TesseractTrigger> triggerList = triggersPage.getRecords();
        List<TesseractTrigger> updateTriggerList = Lists.newArrayList();

        if (!CollectionUtils.isEmpty(triggerList)) {

            triggerList.parallelStream().forEach(trigger -> {

                CronExpression cronExpression = null;

                try {
                    cronExpression = new CronExpression(trigger.getCron());
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                TesseractTrigger updateTrigger = new TesseractTrigger();
                updateTrigger.setId(trigger.getId());
                // 计算下一次触发时间 用于更新数据
                updateTrigger.setNextTriggerTime(cronExpression.getTimeAfter(new Date()).getTime());
                updateTrigger.setPrevTriggerTime(System.currentTimeMillis());
                log.info("触发器 {} 下一次执行时间:{}", trigger.getName(), new Date(updateTrigger.getNextTriggerTime()));
                updateTriggerList.add(updateTrigger);
            });

            this.updateBatchById(updateTriggerList);
        }
        return triggerList;
    }

    @Override
    public Integer findIfExistsByWrapper(QueryWrapper<TesseractTrigger> wrapper) {
        return this.baseMapper.findIfExistsByWrapper(wrapper);
    }
}
