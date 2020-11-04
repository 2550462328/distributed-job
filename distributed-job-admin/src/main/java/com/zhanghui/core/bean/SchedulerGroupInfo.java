package com.zhanghui.core.bean;

import com.zhanghui.core.scanner.ExecutorScanner;
import com.zhanghui.core.schedule.SchedulerThread;

/**
 * 按组分类的group内部线程信息
 * @author: ZhangHui
 * @date: 2020/10/26 10:22
 * @version：1.0
 */
public class SchedulerGroupInfo {

    private final SchedulerThread schedulerThread;

    private final ExecutorScanner executorScanner;

    public SchedulerGroupInfo(SchedulerThread schedulerThread, ExecutorScanner executorScanner) {
        this.schedulerThread = schedulerThread;
        this.executorScanner = executorScanner;
    }

    public SchedulerThread getSchedulerThread() {
        return schedulerThread;
    }

    public void startSchedule(){
        if(this.schedulerThread!=null){
            schedulerThread.startThread();
        }
        if(this.executorScanner!=null){
            executorScanner.startThread();
        }
    }

    public void stopSchedule(){
        if(this.schedulerThread!=null){
            schedulerThread.stopThread();
        }
        if(this.executorScanner!=null){
            executorScanner.stopThread();
        }
    }
}
