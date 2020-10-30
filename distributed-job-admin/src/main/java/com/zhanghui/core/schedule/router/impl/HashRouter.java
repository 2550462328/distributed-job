package com.zhanghui.core.schedule.router.impl;


import com.zhanghui.core.schedule.router.IScheduleRouter;
import com.zhanghui.entity.TesseractExecutorDetail;

import java.util.List;

/**
 * hash散列发送
 */
public class HashRouter implements IScheduleRouter {

    @Override
    public TesseractExecutorDetail routerExecutor(List<TesseractExecutorDetail> tesseractExecutorList) {

        return tesseractExecutorList.get(tesseractExecutorList.hashCode() % tesseractExecutorList.size());
    }
}
