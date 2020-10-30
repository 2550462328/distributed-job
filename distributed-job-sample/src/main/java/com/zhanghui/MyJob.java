package com.zhanghui;

import com.zhanghui.core.annotation.SchedulerJob;
import com.zhanghui.core.handler.JobHandler;
import com.zhanghui.core.handler.context.JobClientContext;
import org.springframework.stereotype.Component;

@SchedulerJob(triggerName = "testTrigger",cron = "*/5 * * * * ?")
@Component
public class MyJob implements JobHandler {

    @Override
    public void execute(JobClientContext clientContext) {
        System.out.println("每隔5秒执行一次任务执行开始");
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("每隔5秒执行一次任务执行结束");
    }
}
