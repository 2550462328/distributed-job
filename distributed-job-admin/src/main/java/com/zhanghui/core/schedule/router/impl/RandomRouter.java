package com.zhanghui.core.schedule.router.impl;


import com.zhanghui.core.schedule.router.IScheduleRouter;
import com.zhanghui.entity.TesseractExecutorDetail;

import java.util.List;
import java.util.Random;

public class RandomRouter implements IScheduleRouter {
    @Override
    public TesseractExecutorDetail routerExecutor(List<TesseractExecutorDetail> tesseractExecutorList) {
        return tesseractExecutorList.get(new Random().nextInt(tesseractExecutorList.size()));
    }
}
