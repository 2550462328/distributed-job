package com.zhanghui.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhanghui.entity.TesseractTrigger;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
public interface ITesseractTriggerService extends IService<TesseractTrigger> {
    List<TesseractTrigger> findTriggerWithLock(String groupName, int triggerSize, long time, Integer timeWindowSize);

    Integer findIfExistsByWrapper(QueryWrapper<TesseractTrigger> wrapper);
}
