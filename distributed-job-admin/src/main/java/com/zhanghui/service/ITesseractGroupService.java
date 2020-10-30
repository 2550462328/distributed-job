package com.zhanghui.service;

import com.zhanghui.entity.TesseractGroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
public interface ITesseractGroupService extends IService<TesseractGroup> {
    List<TesseractGroup> listAllIdAndName();
}
