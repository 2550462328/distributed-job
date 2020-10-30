package com.zhanghui.core.schedule.pool;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程池
 *
 * @author: ZhangHui
 * @date: 2020/10/21 10:29
 * @version：1.0
 */
@Slf4j
public class DefaultSchedulerThreadPool implements ISchedulerThreadPool {

    private static final int DEFAULT_THREAD_NUM = 10;

    // 该线程池的最大同时运行的线程数
    private volatile int threadNum;
    private volatile boolean isStop = false;
    private final Object lock = new Object();
    private final AtomicInteger threadIndex = new AtomicInteger(0);

    public DefaultSchedulerThreadPool(int threadNum) {
        this.threadNum = threadNum;
    }

    // 缓存已生成的WorkerThread，减少创建Thread带来的开销
    private List<WorkerThread> availableWorkerList = Collections.synchronizedList(Lists.newArrayList());

    // 记录正在工作的WorkerThread
    private List<WorkerThread> busyWorkerList = Collections.synchronizedList(Lists.newArrayList());

    /**
     * 获取线程池中还可用的线程数
     *
     * @param
     * @return int
     * @author ZhangHui
     * @date 2020/10/22
     */
    @Override
    public int blockGetAvailableThreadNum() {

        if (isStop) {
            log.error("线程池已关闭");
            return 0;
        }

        int count;

        synchronized (lock) {
            while (busyWorkerList.size() == threadNum) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            count = threadNum - busyWorkerList.size();
        }

        return count;
    }

    @Override
    public void runJob(Runnable runnable) {
        if (isStop) {
            log.error("线程池已关闭");
            return;
        }
        synchronized (lock) {
            // todo 当前线程池的设计就是没有空闲线程可用时等待，原ThreadPool的设计是放入Queue中,可以考虑进行优化
            while (busyWorkerList.size() == threadNum) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            WorkerThread workerThread;

            if(availableWorkerList.size() > 0){
                workerThread = availableWorkerList.remove(0);
            }else{
                workerThread = new WorkerThread(availableWorkerList,busyWorkerList);
                workerThread.start();
                busyWorkerList.add(workerThread);
            }
            workerThread.setRunnable(runnable);
        }
    }

    @Override
    public void shutdown() {
        this.isStop = true;
        availableWorkerList.forEach(avaliableThread -> avaliableThread.stopThread());
        //todo 对于busyWorker这里是立即关闭? 要不要考虑个优雅关机？
        busyWorkerList.forEach(busyThread -> busyThread.stopThread());
    }

    @Override
    public void init() {

    }

    @Override
    public void changeSize(Integer threadNum) {
        if (threadNum <= 0) {
            threadNum = DEFAULT_THREAD_NUM;
        }
        if (threadNum >= this.threadNum) {
            this.threadNum = threadNum;
            return;
        }
        synchronized (lock) {
            int num = threadNum - this.threadNum;

            // 优先从空闲队列中剔除
            if (availableWorkerList.size() > num) {
                num -= removeWorker(availableWorkerList, num);
            }
            if (num != 0) {
                removeWorker(busyWorkerList, num);
            }
            this.threadNum = threadNum;
        }
    }

    private int removeWorker(List<WorkerThread> workerThreadList, Integer num) {
        Iterator<WorkerThread> iterator = workerThreadList.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            WorkerThread workerThread = iterator.next();
            //中断shutdown线程
            workerThread.stopThread();
            iterator.remove();
            if (count++ >= num) {
                return num;
            }
        }
        return count;
    }

    private class WorkerThread extends Thread {
        private volatile Runnable runnable;
        private volatile boolean isStop = false;
        private List<WorkerThread> availableWorkerList;
        private List<WorkerThread> busyWorkerList;

        public WorkerThread(List<WorkerThread> availableWorkerList, List<WorkerThread> busyWorkerList) {
            super(String.format("trigger-dispatch-threadpool %d", threadIndex.getAndIncrement()));
            this.availableWorkerList = availableWorkerList;
            this.busyWorkerList = busyWorkerList;
        }

        public void setRunnable(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            while (!isStop) {
                if (runnable == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }

                try {
                    runnable.run();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    runnable = null;
                    // 任务完成，移动到空闲队列，等待被分配下一个任务
                    busyWorkerList.remove(this);
                    availableWorkerList.add(this);

                    //空闲队列有WorkerThread了，通知等待拿到Worker的线程
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }
        }

        public void stopThread() {
            log.info("{} 线程关闭", Thread.currentThread().getName());
            this.isStop = true;
            Thread.currentThread().interrupt();
        }
    }
}
