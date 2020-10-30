package com.zhanghui.core.annotation;

import com.zhanghui.constant.RouterStategyEnum;
import com.zhanghui.constant.TriggerStatusEnum;

import javax.validation.constraints.NotBlank;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 功能描述
 * @author ZhangHui
 * @date 2020/10/26
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerJob {
    @NotBlank String triggerName();

    String groupName() default "";

    /**
     * cron表达式
     */
    String cron() default "";

    /**
     * trigger描述信息
     */
    String desc() default "";

    /**
     * 集群环境下执行的路由策略
     */
    RouterStategyEnum strategy() default RouterStategyEnum.SCHEDULER_STRATEGY_RANDOM;

    /**
     * 失败重试次数，默认0 为不重试
     */
    int retryTimes() default 0;

    /**
     * 分片数
     */
    int shardingNum() default 1;

    /**
     * 是否留存日志记录
     */
    boolean isLog() default false;

    /**
     * 开启状态，默认开启
     */
    TriggerStatusEnum status() default TriggerStatusEnum.TRGGER_STATUS_STARTING;
}
