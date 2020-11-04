package com.zhanghui.core.dto;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;

@Data
@ToString
public class TesseractAdminJobDetailDTO {
    @NotBlank
    private String className;
    @NotBlank
    private String triggerName;
    @NotBlank
    private String groupName;

    private String cron;

    private String desc;

    private Integer strategy;

    private Integer retryTimes;

    private Integer shardingNum;

    private Boolean isLog;

    private Integer status;
}
