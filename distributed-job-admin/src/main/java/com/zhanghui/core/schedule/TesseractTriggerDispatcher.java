package com.zhanghui.core.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.bean.TaskContextInfo;
import com.zhanghui.core.executor.TaskExecuteDelegator;
import com.zhanghui.core.schedule.pool.DefaultSchedulerThreadPool;
import com.zhanghui.core.schedule.pool.ISchedulerThreadPool;
import com.zhanghui.entity.*;
import feignService.IAdminFeignService;
import com.zhanghui.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import java.util.List;

/**
 * @author: ZhangHui
 * @date: 2020/10/21 10:21
 * @version：1.0
 */
@Slf4j
public class TesseractTriggerDispatcher {

    private final TesseractGroup tesseractGroup;
    private final ITesseractJobDetailService tesseractJobDetailService = JobServiceDelegator.jobDetailService;;
    private final ITesseractExecutorDetailService executorDetailService = JobServiceDelegator.executorDetailService;

    private final ISchedulerThreadPool threadPool;

    public TesseractTriggerDispatcher(TesseractGroup tesseractGroup){
        this.tesseractGroup = tesseractGroup;
        threadPool = new DefaultSchedulerThreadPool(tesseractGroup.getThreadPoolNum());
    }

    public ISchedulerThreadPool getThreadPool() {
        return threadPool;
    }

    public void init(){
        threadPool.init();
    }

    public int blockGetAvailableThreadNum() {
        return threadPool.blockGetAvailableThreadNum();
    }

    public void dispatchTrigger(List<TesseractTrigger> triggerList) {
        triggerList.stream().forEach(trigger -> threadPool.runJob(new TaskRunnable(trigger)));
    }

    class TaskRunnable implements Runnable{

        private final TesseractTrigger tesseractTrigger;

        public TaskRunnable(TesseractTrigger tesseractTrigger){
            this.tesseractTrigger = tesseractTrigger;
        }

        @Override
        public void run() {

            QueryWrapper<TesseractJobDetail> jobQueryWrapper = new QueryWrapper<>();

            jobQueryWrapper.lambda().eq(TesseractJobDetail::getTriggerId,tesseractTrigger.getId());
            List<TesseractJobDetail> jobDetailList = tesseractJobDetailService.list(jobQueryWrapper);

            if(CollectionUtils.isEmpty(jobDetailList)){
                return;
            }

            QueryWrapper<TesseractExecutorDetail> executorDetailQueryWrapper = new QueryWrapper<>();
            executorDetailQueryWrapper.lambda().eq(TesseractExecutorDetail::getGroupId,tesseractTrigger.getGroupId());
            List<TesseractExecutorDetail> executorDetailList = executorDetailService.list(executorDetailQueryWrapper);

            if (CollectionUtils.isEmpty(executorDetailList)) {
                return;
            }

            //路由发送执行(负载策略)
            TaskContextInfo taskContextInfo = TaskExecuteDelegator.createTaskContextInfo(tesseractTrigger,executorDetailList,jobDetailList);

            TaskExecuteDelegator.routerExecute(taskContextInfo);
        }

    }

    public void stop() {
        threadPool.shutdown();
    }
}
