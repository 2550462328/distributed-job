package com.zhanghui.core;

import com.zhanghui.core.cache.TriggerMongoCache;
import com.zhanghui.core.cache.TriggerRedisCache;
import com.zhanghui.service.*;
import feignService.IAdminFeignService;

/**
 * 保留admin端service的bean类，不对原有代码进行入侵
 *
 * @author: ZhangHui
 * @date: 2020/10/26 10:34
 * @version：1.0
 */

public class JobServiceDelegator {

    public static ITesseractGroupService groupService;

    public static ITesseractTriggerService triggerService;

    public static ITesseractExecutorDetailService executorDetailService;

    public static ITesseractJobDetailService jobDetailService;

    public static ITesseractLogService logService;

    public static ITesseractFiredTriggerService firedTriggerService;

    public static IAdminFeignService feignService;

    public static TriggerMongoCache triggerMongoCache;
}
