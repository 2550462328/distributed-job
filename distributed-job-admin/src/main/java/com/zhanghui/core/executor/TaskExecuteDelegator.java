package com.zhanghui.core.executor;

import com.zhanghui.core.bean.TaskContextInfo;
import com.zhanghui.core.bean.TaskInfo;
import com.zhanghui.core.schedule.router.IScheduleRouter;
import com.zhanghui.core.schedule.router.impl.RandomRouter;
import com.zhanghui.entity.TesseractExecutorDetail;
import com.zhanghui.entity.TesseractJobDetail;
import com.zhanghui.entity.TesseractTrigger;
import com.zhanghui.exception.TesseractException;
import com.zhanghui.util.threadpool.ThreadPoolFactoryUtils;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static com.zhanghui.constant.AdminConstant.*;

/**
 * 执行task的委托类
 *
 * @author: ZhangHui
 * @date: 2020/10/26 14:18
 * @version：1.0
 */
public class TaskExecuteDelegator {

    private static final String THREADPOOL_NAME_PREFIX = "job-executor-threadpool-";

    // todo 目前用一个线程池执行所有Group下所有trigger相关联的jobDetail
    private static final ExecutorService TRIGGER_EXCUTE_THREAD_POOL = ThreadPoolFactoryUtils.createCustomThreadPoolIfAbsent(THREADPOOL_NAME_PREFIX);

    /**
     * 路由执行器
     *
     * @param taskContextInfo 任务上下文
     */
    public static void routerExecute(TaskContextInfo taskContextInfo) {
        TesseractTrigger trigger = taskContextInfo.getTrigger();
        //路由选择
        @NotNull Integer strategy = trigger.getStrategy();

        if (SCHEDULER_STRATEGY_BROADCAST.equals(strategy)) {
            executeBroadcast(taskContextInfo);
        } else if (SCHEDULER_STRATEGY_SHARDING.equals(strategy)) {
            executeSharding(taskContextInfo);
        } else {
            //正常调用
            executeGeneral(taskContextInfo);
        }
    }

    /**
     * 正常调用
     *
     * @param taskContextInfo
     */
    private static void executeGeneral(TaskContextInfo taskContextInfo) {
        taskContextInfo.getJobDetailList().forEach(jobDetail -> {
            TesseractExecutorDetail executorDetail = routeExecutorDetail(taskContextInfo,null);

            if (executorDetail != null) {
                TaskInfo taskInfo = createTaskInfo(taskContextInfo.getTrigger(), executorDetail, jobDetail);

                TRIGGER_EXCUTE_THREAD_POOL.execute(new TriggerExecuteThread(taskContextInfo,taskInfo));
            }
        });
    }

    /**
     * 通过路由规则获得执行器
     * 为了保证两次获取不能取相同值，使用preExecutor保存上一次获取的结果
     */
    public static TesseractExecutorDetail routeExecutorDetail(TaskContextInfo taskContextInfo, TesseractExecutorDetail preExecutor){

        IScheduleRouter scheduleRouter = SCHEDULE_ROUTER_MAP.getOrDefault(taskContextInfo.getTrigger().getStrategy(), new RandomRouter());
        TesseractExecutorDetail executorDetail = scheduleRouter.routerExecutor(taskContextInfo.getExecutorDetailList());

        List<TesseractExecutorDetail> executorDetailList = taskContextInfo.getExecutorDetailList();

        if(executorDetailList.size() == 0){
            return null;
        }else if(executorDetailList.size() == 1){
            return executorDetailList.get(0);
        }else if(executorDetail == preExecutor){
            executorDetail = scheduleRouter.routerExecutor(taskContextInfo.getExecutorDetailList());
        }
        return executorDetail;
    }

    /**
     * 广播调用
     *
     * @param taskContextInfo
     */
    private static void executeBroadcast(TaskContextInfo taskContextInfo) {

        taskContextInfo.getJobDetailList().forEach(jobDetail -> {
            taskContextInfo.getExecutorDetailList().forEach(executorDetail -> {
                TaskInfo taskInfo = createTaskInfo(taskContextInfo.getTrigger(), executorDetail, jobDetail);
                TRIGGER_EXCUTE_THREAD_POOL.execute(new TriggerExecuteThread(taskContextInfo,taskInfo));
            });
        });
    }

    /**
     * 分片调用
     *
     * @param taskContextInfo
     */
    private static void executeSharding(TaskContextInfo taskContextInfo) {

        TesseractTrigger trigger = taskContextInfo.getTrigger();
        List<TesseractExecutorDetail> executorDetailList = taskContextInfo.getExecutorDetailList();
        @NotNull Integer shardingNum = trigger.getShardingNum();
        if (shardingNum == 0) {
            throw new TesseractException("分片数不能等于0");
        }
        int size = executorDetailList.size();
        int count = 0;
        for (int i = 0; i < shardingNum; i++) {
            //轮询发送给执行器执行
            if (count >= size) {
                count = 0;
            }
            count++;
            TesseractExecutorDetail executorDetail = executorDetailList.get(count);

            if (executorDetail != null) {
                taskContextInfo.getJobDetailList().forEach(jobDetail -> {
                    TaskInfo taskInfo = createTaskInfo(taskContextInfo.getTrigger(), executorDetail, jobDetail);
                    TRIGGER_EXCUTE_THREAD_POOL.execute(new TriggerExecuteThread(taskContextInfo,taskInfo));
                });
            }
        }
    }

    /**
     * 停止执行
     */
    public static void stopExecute() {
        TRIGGER_EXCUTE_THREAD_POOL.shutdown();
    }

    //############################################ 静态方法 ###################################################

    public static TaskContextInfo createTaskContextInfo(TesseractTrigger tesseractTrigger, List<TesseractExecutorDetail> executorDetailList, List<TesseractJobDetail> jobDetailList) {
        TaskContextInfo taskContextInfo = TaskContextInfo.builder()
                .executorDetailList(executorDetailList)
                .jobDetailList(jobDetailList)
                .trigger(tesseractTrigger).build();

        return taskContextInfo;
    }

    public static TaskInfo createTaskInfo(TesseractTrigger tesseractTrigger, TesseractExecutorDetail executorDetail, TesseractJobDetail jobDetail) {
        TaskInfo taskInfo = TaskInfo.builder()
                .executorDetail(executorDetail)
                .jobDetail(jobDetail)
                .trigger(tesseractTrigger).build();

        return taskInfo;
    }
}
