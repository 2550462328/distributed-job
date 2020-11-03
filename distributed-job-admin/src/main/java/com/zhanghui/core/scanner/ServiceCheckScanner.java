package com.zhanghui.core.scanner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.zhanghui.core.JobServerBootstrap;
import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.cache.ExecutorDetailCache;
import com.zhanghui.core.cache.TriggerMongoCache;
import com.zhanghui.core.cache.TriggerRedisCache;
import com.zhanghui.core.lifestyle.IThreadLifycycle;
import com.zhanghui.entity.TesseractExecutorDetail;
import com.zhanghui.entity.TesseractGroup;
import com.zhanghui.entity.TesseractTrigger;
import com.zhanghui.mapper.TesseractGroupMapper;
import com.zhanghui.service.ITesseractExecutorDetailService;
import com.zhanghui.service.ITesseractGroupService;
import com.zhanghui.service.ITesseractTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 定期清除没有绑定trigger的group，毕竟一个group相当于两个不断执行的thread啊
 *
 * @author: ZhangHui
 * @date: 2020/10/22 13:56
 * @version：1.0
 */
@Slf4j
public class ServiceCheckScanner extends Thread implements IThreadLifycycle {

    private volatile boolean isStop = false;

    // 执行间隔5分钟
    private static final Integer SCAN_INTERVAL_TIME = 5 * 60 * 1000;
    private static final String THREAD_NAME = "serviceCheckScannerThread";

    private final ITesseractGroupService tesseractGroupService = JobServiceDelegator.groupService;
    private final ITesseractTriggerService tesseractTriggerService = JobServiceDelegator.triggerService;
    private final TriggerMongoCache triggerMongoCache = JobServiceDelegator.triggerMongoCache;

    public ServiceCheckScanner() {
        super(THREAD_NAME);
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
        log.info("serviceCheckScannerThread start...");

        List<Integer> removeGroupIds = Lists.newArrayList();
        List<String> removeGroupNames = Lists.newArrayList();

        while (!isStop) {
            List<TesseractGroup> groups = tesseractGroupService.listAllIdAndName();

            QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();

            if (!CollectionUtils.isEmpty(groups)) {
                groups.stream().forEach(group -> {
                    queryWrapper.lambda().eq(TesseractTrigger::getGroupId, group.getId());
                    Integer exists = tesseractTriggerService.findIfExistsByWrapper(queryWrapper);

                    if (exists == null) {
                        removeGroupIds.add(group.getId());
                        removeGroupNames.add(group.getName());
                    }
                    queryWrapper.clear();
                });

                if (!CollectionUtils.isEmpty(removeGroupIds)) {
                    log.info("发现未绑定trigger的group [{}]，即将移除", removeGroupNames);
                    tesseractGroupService.removeByIds(removeGroupIds);

                    removeGroupNames.forEach(groupName->{
                        // 删除group下的groupScheduler线程组
                        JobServerBootstrap.deleteGroupScheduler(groupName);

                        // 删除group下的mongodb缓存
                        triggerMongoCache.removeAllTriggerFromCache(groupName);
                    });
                    removeGroupIds.clear();
                    removeGroupNames.clear();
                }
            }

            try {
                // 间隔5分钟执行一次
                Thread.sleep(SCAN_INTERVAL_TIME);
            } catch (InterruptedException e) {
                log.error("serviceCheckScannerThread 在sleep期间被interrupt：[{}]", e.getMessage());
            }
        }
    }

    @Override
    public void stopThread() {
        isStop = true;
        this.interrupt();
    }
}
