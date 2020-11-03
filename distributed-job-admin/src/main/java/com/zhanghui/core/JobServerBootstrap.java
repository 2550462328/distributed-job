package com.zhanghui.core;

import com.zhanghui.core.bean.SchedulerGroupInfo;
import com.zhanghui.core.cache.ExecutorDetailCache;
import com.zhanghui.core.cache.TriggerMongoCache;
import com.zhanghui.core.cache.TriggerRedisCache;
import com.zhanghui.core.executor.TaskExecuteDelegator;
import com.zhanghui.core.scanner.ExecutorScanner;
import com.zhanghui.core.scanner.ServiceCheckScanner;
import com.zhanghui.core.schedule.SchedulerThread;
import com.zhanghui.core.schedule.TesseractTriggerDispatcher;
import com.zhanghui.core.schedule.pool.ISchedulerThreadPool;
import com.zhanghui.entity.TesseractGroup;
import com.zhanghui.entity.TesseractTrigger;
import com.zhanghui.exception.TesseractException;
import com.zhanghui.service.*;
import feignService.IAdminFeignService;
import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author: ZhangHui
 * @date: 2020/10/20 16:51
 * @version：1.0
 */
@Slf4j
public class JobServerBootstrap {

    @Autowired
    private ITesseractGroupService groupService;

    @Autowired
    private ITesseractTriggerService triggerService;

    @Autowired
    private ITesseractExecutorDetailService executorDetailService;

    @Autowired
    private ITesseractJobDetailService jobDetailService;

    @Autowired
    private ITesseractLogService logService;

    @Autowired
    private ITesseractFiredTriggerService firedTriggerService;

    @Autowired
    private IAdminFeignService feignService;

    @Autowired
    private TriggerMongoCache triggerMongoCache;

    private ServiceCheckScanner serviceCheckScanner;

    private static final Map<String, SchedulerGroupInfo> SCHEDULER_GROUP_MAP = new ConcurrentHashMap<>();

    private static final ExecutorDetailCache EXECUTORDETAIL_CACHE = new ExecutorDetailCache();

    private static final ReentrantReadWriteLock GROUP_INFO_READWRITE_LOCK = new ReentrantReadWriteLock();
    private static final ReentrantReadWriteLock.WriteLock GROUP_INFO_WRITE_LOCK = GROUP_INFO_READWRITE_LOCK.writeLock();
    private static final ReentrantReadWriteLock.ReadLock GROUP_INFO_READ_LOCK = GROUP_INFO_READWRITE_LOCK.readLock();

    public ExecutorDetailCache getExecutorDetailCache() {
        return EXECUTORDETAIL_CACHE;
    }

    /***********************************spring生命周期  start *************************************/
    /**
     * 监听ContextRefreshedEvent事件
     */
    @EventListener(ContextRefreshedEvent.class)
    public void start(){
        SCHEDULER_GROUP_MAP.values().forEach(schedulerGroup -> {
            schedulerGroup.startSchedule();
        });
    }

    public void init() {
        initServiceDelegator();
        initSchedulerGroupInfo();
        initServiceCheck();

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            log.info("程序正在关闭，执行自定义关闭策略");
            this.destroy();
        }));
    }

    private void initServiceDelegator(){
        JobServiceDelegator.executorDetailService = executorDetailService;
        JobServiceDelegator.firedTriggerService = firedTriggerService;
        JobServiceDelegator.groupService = groupService;
        JobServiceDelegator.jobDetailService = jobDetailService;
        JobServiceDelegator.logService = logService;
        JobServiceDelegator.triggerService = triggerService;
        JobServiceDelegator.feignService = feignService;
        JobServiceDelegator.triggerMongoCache = triggerMongoCache;
    }

    private void initSchedulerGroupInfo() {

        // 分组进行线程池隔离
        List<TesseractGroup> groups = groupService.list();

        if (!CollectionUtils.isEmpty(groups)) {
            groups.forEach(group -> {
                SchedulerGroupInfo schedulerGroupInfo = buildSchedulerGroup(group);
                if(schedulerGroupInfo != null) {
                    SCHEDULER_GROUP_MAP.put(group.getName(), schedulerGroupInfo);
                }
            });
        }
    }

    private void initServiceCheck(){
        if(serviceCheckScanner != null && serviceCheckScanner.isAlive()){
            return;
        }else{
            serviceCheckScanner = new ServiceCheckScanner();
            serviceCheckScanner.setDaemon(true);
            serviceCheckScanner.startThread();
        }
    }

    private static SchedulerGroupInfo buildSchedulerGroup(TesseractGroup group) {
        if(group == null || group.getThreadPoolNum() == 0){
            return null;
        }

        // 每一个组创建一个trigger调度器，可以理解成一个任务调度器，里面有一个线程池，负责处理trigger的任务
        TesseractTriggerDispatcher tesseractTriggerDispatcher = new TesseractTriggerDispatcher(group);

        // 每一组创建一个扫描线程，就是根据组名查找trigger，将trigger放到tesseractTriggerDispatcher里面去执行
        SchedulerThread schedulerThread = new SchedulerThread(group,tesseractTriggerDispatcher);
        schedulerThread.setDaemon(true);

        ExecutorScanner executorScanner = new ExecutorScanner(EXECUTORDETAIL_CACHE);
        executorScanner.setDaemon(true);

        SchedulerGroupInfo schedulerGroupInfo = new SchedulerGroupInfo(schedulerThread,executorScanner);

        return schedulerGroupInfo;
    }

    public void destroy(){
        SCHEDULER_GROUP_MAP.values().forEach(schedulerGroup -> {
            schedulerGroup.stopSchedule();
        });
        EXECUTORDETAIL_CACHE.clear();
        TaskExecuteDelegator.stopExecute();
    }

    /***********************************spring生命周期  end*************************************/


    /***********************************静态工具方法  start *************************************/

    /**
     * 删除组线程池并停止
     *
     * @param
     */
    public static void deleteGroupScheduler(TesseractGroup tesseractGroup) {
        GROUP_INFO_WRITE_LOCK.lock();
        try {
            SchedulerGroupInfo scheduleGroupInfo = SCHEDULER_GROUP_MAP.remove(tesseractGroup.getName());
            scheduleGroupInfo.stopSchedule();
        } finally {
            GROUP_INFO_WRITE_LOCK.unlock();
        }
        log.info("删除组调度器{}成功,删除结果:{}", tesseractGroup, SCHEDULER_GROUP_MAP);
    }

    /**
     * 删除组线程池并停止
     *
     * @param
     */
    public static void deleteGroupScheduler(String groupName) {
        GROUP_INFO_WRITE_LOCK.lock();
        try {
            SchedulerGroupInfo scheduleGroupInfo = SCHEDULER_GROUP_MAP.remove(groupName);
            scheduleGroupInfo.stopSchedule();
        } finally {
            GROUP_INFO_WRITE_LOCK.unlock();
        }
        log.info("删除组调度器{}成功,删除结果:{}", groupName, SCHEDULER_GROUP_MAP);
    }

    /**
     * 增加组线程池
     *
     * @param
     */
    public static void addGroupScheduler(TesseractGroup tesseractGroup) {
        GROUP_INFO_WRITE_LOCK.lock();
        try {
            SchedulerGroupInfo scheduleGroupInfo = buildSchedulerGroup(tesseractGroup);
            SCHEDULER_GROUP_MAP.put(tesseractGroup.getName(), scheduleGroupInfo);
            scheduleGroupInfo.startSchedule();
        } finally {
            GROUP_INFO_WRITE_LOCK.unlock();
        }
        log.info("添加组调度器{}成功,添加结果:{}", tesseractGroup, SCHEDULER_GROUP_MAP);
    }

    /**
     * 执行触发器
     *
     * @param group
     * @param tesseractTriggerList
     */
    public static void executeTrigger(TesseractGroup group, List<TesseractTrigger> tesseractTriggerList) {
        GROUP_INFO_READ_LOCK.lock();
        SchedulerGroupInfo scheduleGroupInfo = SCHEDULER_GROUP_MAP.get(group.getId());
        try {
            if (scheduleGroupInfo == null) {
                throw new TesseractException(String.format("找不到组: %s 的调度线程，请先设置调度线程", group.getName()));
            }
            SchedulerThread schedulerThread = scheduleGroupInfo.getSchedulerThread();
            TesseractTriggerDispatcher tesseractTriggerDispatcher = schedulerThread.getTesseractTriggerDispatcher();
            tesseractTriggerDispatcher.dispatchTrigger(tesseractTriggerList);
        } finally {
            GROUP_INFO_READ_LOCK.unlock();
        }
    }

    /**
     * 更新执行线程池大小
     *
     * @param group
     * @param threadNum
     */
    public static void updateThreadNum(TesseractGroup group, Integer threadNum) {
        GROUP_INFO_WRITE_LOCK.lock();
        SchedulerGroupInfo scheduleGroupInfo = SCHEDULER_GROUP_MAP.get(group.getId());
        try {
            SchedulerThread schedulerThread;
            if (scheduleGroupInfo == null) {
                log.warn("找不到组:{} SchedulerThread", group.getName());
                //不存在添加
                addGroupScheduler(group);
                return;
            }
            schedulerThread = scheduleGroupInfo.getSchedulerThread();
            ISchedulerThreadPool threadPool = schedulerThread.getTesseractTriggerDispatcher().getThreadPool();
            threadPool.changeSize(threadNum);
        } finally {
            GROUP_INFO_WRITE_LOCK.unlock();
        }
    }

    /***********************************静态工具方法  end *************************************/
}
