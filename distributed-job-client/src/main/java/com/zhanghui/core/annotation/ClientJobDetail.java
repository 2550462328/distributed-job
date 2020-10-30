package com.zhanghui.core.annotation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientJobDetail {
    private String className;
    private String triggerName;
    private String groupName;
    private String cron;
    private String desc;
    private Integer strategy;
    private Integer retryTimes;
    private Integer shardingNum;
    private Boolean isLog;
    private Integer status;
}
