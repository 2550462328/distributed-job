package com.zhanghui.core.scanner;

import com.zhanghui.core.JobServiceDelegator;
import com.zhanghui.core.cache.ExecutorDetailCache;
import com.zhanghui.core.lifestyle.IThreadLifycycle;
import com.zhanghui.entity.TesseractExecutorDetail;
import com.zhanghui.service.ITesseractExecutorDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: ZhangHui
 * @date: 2020/10/22 13:56
 * @version：1.0
 */
@Slf4j
public class ExecutorScanner extends Thread implements IThreadLifycycle {

    private volatile boolean isStop = false;
    private static final Integer SCAN_INTERVAL_TIME = 15 * 1000;
    private static final String THREAD_NAME = "executorScannerThread";

    private final ITesseractExecutorDetailService executorDetailService = JobServiceDelegator.executorDetailService;

    private final ExecutorDetailCache executorDetailCache;

    public ExecutorScanner(ExecutorDetailCache executorDetailCache) {
        super(THREAD_NAME);
        this.executorDetailCache = executorDetailCache;
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
        log.info("ExecutorScannerThread Start...");
        while (!isStop) {
            List<TesseractExecutorDetail> inValidExecutos = executorDetailService.listInvalid();

            if (!CollectionUtils.isEmpty(inValidExecutos)) {
                List<Integer> inValidExecutorIds = inValidExecutos.stream().map(executorDetail -> executorDetail.getId()).collect(Collectors.toList());
                log.info("发现失效的机器 {},执行移除操作", inValidExecutos);
                // 先删除数据库，再删除缓存
                executorDetailService.removeByIds(inValidExecutorIds);
                inValidExecutos.forEach(executorDetail -> executorDetailCache.removeCacheExecutor(executorDetail.getSocket()));
            }

            try {
                // 间隔15s执行一次
                Thread.sleep(SCAN_INTERVAL_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stopThread() {
        isStop = true;
        this.interrupt();
    }
}
