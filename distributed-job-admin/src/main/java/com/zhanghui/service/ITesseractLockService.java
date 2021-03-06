package com.zhanghui.service;

import com.zhanghui.entity.TesseractLock;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
public interface ITesseractLockService extends IService<TesseractLock> {
    TesseractLock lock(String lockName, String groupName);
}
