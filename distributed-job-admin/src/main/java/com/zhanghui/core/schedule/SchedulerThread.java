package com.zhanghui.core.schedule;

import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.lifestyle.IThreadLifycycle;
import com.zhanghui.core.schedule.TesseractTriggerDispatcher;
import com.zhanghui.entity.TesseractGroup;
import com.zhanghui.entity.TesseractTrigger;
import com.zhanghui.service.ITesseractTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Random;

/**
 * 扫描线程，扫描指定组名下的trigger，并放到TriggerDispatcher中执行
 * <p>
 * 使用任务定时运行的核心逻辑
 *
 * @author: ZhangHui
 * @date: 2020/10/21 10:43
 * @version：1.0
 */
@Slf4j
public class SchedulerThread extends Thread implements IThreadLifycycle {
    /**
     * 时间窗口，获取距下次触发时间xs内的触发器
     */
    private static final int TIME_WINDOW_SIZE = 0 * 1000;

    /**
     * 调度最大间隔时间
     */
    private final int MAX_SLEEP_TIME = 20 * 1000;

    /**
     * 功能描述
     */
    private final int MIN_SLEEP_TIME = 1 * 1000;

    private Random sleepRandom = new Random();
    /**
     * 触发容错时间
     */
    private final int ACCURATE_TIME = 1 * 1000;

    private final ITesseractTriggerService tesseractTriggerService = JobServiceDelegator.triggerService;
    private final TesseractTriggerDispatcher tesseractTriggerDispatcher;
    private final TesseractGroup tesseractGroup;

    private volatile boolean isStop = false;

    public SchedulerThread(TesseractGroup tesseractGroup, TesseractTriggerDispatcher tesseractTriggerDispatcher) {
        super(String.format("SchedulerThread-%s", tesseractGroup.getName()));
        this.tesseractGroup = tesseractGroup;
        this.tesseractTriggerDispatcher = tesseractTriggerDispatcher;
    }

    public TesseractTriggerDispatcher getTesseractTriggerDispatcher() {
        return tesseractTriggerDispatcher;
    }

    @Override
    public void run() {
        log.info("SchedulerThread {} start", tesseractGroup.getName());
        while (!isStop) {
            int blockGetAvailableThreadnum = tesseractTriggerDispatcher.blockGetAvailableThreadNum();

            List<TesseractTrigger> triggerList = tesseractTriggerService.findTriggerWithLock(tesseractGroup.getName(), blockGetAvailableThreadnum, System.currentTimeMillis(), TIME_WINDOW_SIZE);

            if (!CollectionUtils.isEmpty(triggerList)) {
                // 选取最近触发的一个trigger
                TesseractTrigger latestTrigger = triggerList.get(0);
                Long nextTriggerTime = latestTrigger.getNextTriggerTime();
                long gapTime = nextTriggerTime - System.currentTimeMillis();
                // 如果触发时间还早（大于我们预设的最短时间），则让线程休息去吧
                if (gapTime > ACCURATE_TIME) {
                    synchronized (this) {
                        try {
                            this.wait(gapTime);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // 否则，或者线程唤醒 ,交由任务调度器执行
                tesseractTriggerDispatcher.dispatchTrigger(triggerList);
                continue;
            }

            try {
                // 随机睡眠
//                Thread.sleep(nextScheduleTime());
                Thread.sleep(MIN_SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void initThread() {

    }

    @Override
    public void startThread() {
        this.start();
    }

    @Override
    public void stopThread() {
        this.tesseractTriggerDispatcher.stop();
        this.isStop = true;
        this.interrupt();
    }

    /**
     * 返回下一次调度时间，最低为2s
     *
     * @return
     */
    private long nextScheduleTime() {
        long sleepTime = sleepRandom.nextInt(this.MAX_SLEEP_TIME);
        if (sleepTime < MIN_SLEEP_TIME) {
            sleepTime = MIN_SLEEP_TIME;
        }
        return sleepTime;
    }
}
