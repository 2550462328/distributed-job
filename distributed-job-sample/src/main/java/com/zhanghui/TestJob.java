package com.zhanghui;

import com.zhanghui.constant.RouterStategyEnum;
import com.zhanghui.constant.TriggerStatusEnum;
import com.zhanghui.core.annotation.SchedulerJob;
import com.zhanghui.core.handler.JobHandler;
import com.zhanghui.core.handler.context.JobClientContext;
import org.springframework.stereotype.Component;

@SchedulerJob(triggerName = "testTrigger1",cron = "*/20 * * * * ?",isLog = true,strategy = RouterStategyEnum.SCHEDULER_STRATEGY_LOADFACTOR,status = TriggerStatusEnum.TRGGER_STATUS_STARTING)
@Component
public class TestJob implements JobHandler {

    @Override
    public void execute(JobClientContext clientContext) {
        System.out.println("每隔20秒执行一次任务执行开始");
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("每隔20秒执行一次任务执行结束");
    }
}
