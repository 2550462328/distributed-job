package com.zhanghui.core.schedule.router;


import com.zhanghui.entity.TesseractExecutorDetail;

import java.util.List;


public interface IScheduleRouter {
    TesseractExecutorDetail routerExecutor(List<TesseractExecutorDetail> tesseractExecutorDetailList);
}
