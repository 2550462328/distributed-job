package com.zhanghui.core.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class TesseractExecutorRequest {
    @NotNull
    private String className;
    @NotNull
    private Integer shardingIndex;

    private Integer logId;

    @NotNull
    private Integer triggerId;
}
