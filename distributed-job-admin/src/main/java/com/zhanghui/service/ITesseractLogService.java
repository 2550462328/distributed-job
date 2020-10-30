package com.zhanghui.service;

import com.zhanghui.core.dto.TesseractAdminJobNotify;
import com.zhanghui.entity.TesseractLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
public interface ITesseractLogService extends IService<TesseractLog> {
    void notify(TesseractAdminJobNotify tesseractAdminJobNotify);
}
