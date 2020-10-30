package com.zhanghui.core.bean;

import com.zhanghui.entity.TesseractExecutorDetail;
import com.zhanghui.entity.TesseractJobDetail;
import com.zhanghui.entity.TesseractTrigger;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 执行任务的列表信息
 * @author: ZhangHui
 * @date: 2020/10/26 14:04
 * @version：1.0
 */
@Getter
@Setter
@Builder
public class TaskContextInfo {
    /**
     * 任务详情列表
     */
    private List<TesseractJobDetail> jobDetailList;
    /**
     * 执行器列表
     */
    private List<TesseractExecutorDetail> executorDetailList;
    /**
     * 触发器
     */
    private TesseractTrigger trigger;
}
