package com.zhanghui.core.schedule.router.impl;


import com.zhanghui.core.schedule.router.IScheduleRouter;
import com.zhanghui.entity.TesseractExecutorDetail;

import java.util.List;

/**
 * 不操作，直接返回空或者抛出异常
 */
public class PollingRouter implements IScheduleRouter {

    @Override
    public TesseractExecutorDetail routerExecutor(List<TesseractExecutorDetail> tesseractExecutorList) {
        throw new RuntimeException("不支持的操作");
    }
}
