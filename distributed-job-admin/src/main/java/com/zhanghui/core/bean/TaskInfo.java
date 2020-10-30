package com.zhanghui.core.bean;

import com.zhanghui.entity.TesseractExecutorDetail;
import com.zhanghui.entity.TesseractJobDetail;
import com.zhanghui.entity.TesseractTrigger;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

/**
 * 执行任务的详细信息
 *
 * @author: ZhangHui
 * @date: 2020/10/27 11:00
 * @version：1.0
 */
@Builder
@Getter
public class TaskInfo {
    /**
     * 任务详情
     */
    @NotBlank  private TesseractJobDetail jobDetail;
    /**
     * 任务执行器
     */
    @NotBlank  private TesseractExecutorDetail executorDetail;
    /**
     * 触发器
     */
    @NotBlank private TesseractTrigger trigger;
}
