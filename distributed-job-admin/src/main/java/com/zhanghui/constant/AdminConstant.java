package com.zhanghui.constant;

import com.zhanghui.core.schedule.router.IScheduleRouter;
import com.zhanghui.core.schedule.router.impl.HashRouter;
import com.zhanghui.core.schedule.router.impl.LoadFactorRouter;
import com.zhanghui.core.schedule.router.impl.PollingRouter;
import com.zhanghui.core.schedule.router.impl.RandomRouter;

import java.util.HashMap;
import java.util.Map;

public class AdminConstant {
    /**
     * 用户相关
     */
    public static final Integer USER_VALID = 1;
    public static final Integer USER_INVALID = 0;
    /**
     * 日志相关
     */
    public static final String NULL_SOCKET = "";
    public static final Integer LOG_NO_CONFIRM = 3;
    public static final Integer LOG_WAIT = 2;
    public static final Integer LOG_SUCCESS = 1;
    public static final Integer LOG_FAIL = 0;
    /**
     * 锁相关
     */
    public static final String TRIGGER_LOCK_NAME = "TRIGGER_ACCESS";
    public static final String JOB_LOCK_NAME = "JOB_ACCESS";
    /**
     * 调度策略
     */
    public static final Integer SCHEDULER_STRATEGY_RANDOM= 0;
    public static final Integer SCHEDULER_STRATEGY_HASH = 1;
    public static final Integer SCHEDULER_STRATEGY_LOADFACTOR = 2;
    public static final Integer SCHEDULER_STRATEGY_POLLING = 3;
    public static final Integer SCHEDULER_STRATEGY_BROADCAST = 4;
    public static final Integer SCHEDULER_STRATEGY_SHARDING = 5;

    public static final Map<Integer, IScheduleRouter> SCHEDULE_ROUTER_MAP = new HashMap<Integer, IScheduleRouter>() {
        {
            put(SCHEDULER_STRATEGY_HASH, new HashRouter());
            put(SCHEDULER_STRATEGY_RANDOM, new RandomRouter());
            put(SCHEDULER_STRATEGY_LOADFACTOR, new LoadFactorRouter());
            put(SCHEDULER_STRATEGY_POLLING, new PollingRouter());
        }
    };
    /**
     * 触发器状态
     */
    public static final Integer TRGGER_STATUS_STOPING = 0;
    public static final Integer TRGGER_STATUS_STARTING = 1;
}
