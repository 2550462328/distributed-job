package com.zhanghui.core.schedule.router.impl;


import com.zhanghui.core.schedule.router.IScheduleRouter;
import com.zhanghui.entity.TesseractExecutorDetail;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
public class LoadFactorRouter implements IScheduleRouter {

    @Override
    public TesseractExecutorDetail routerExecutor(List<TesseractExecutorDetail> tesseractExecutorDetailList) {
        List<TesseractExecutorDetail> tesseractExecutorDetailList_new = new ArrayList<>();
        tesseractExecutorDetailList.forEach(executorDetail -> {
            int repeatTime = (int) (1 / (1 - executorDetail.getLoadFactor()));
            for (int i = 0; i < repeatTime; i++) {
                tesseractExecutorDetailList_new.add(executorDetail);
            }
        });
        return tesseractExecutorDetailList_new.get(new Random().nextInt(tesseractExecutorDetailList_new.size()));
    }
}
