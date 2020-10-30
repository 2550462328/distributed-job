package com.zhanghui.service;

import com.zhanghui.core.dto.TesseractAdminRegistryRequest;
import com.zhanghui.core.dto.TesseractAdminRegistryResDTO;
import com.zhanghui.entity.TesseractJobDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
public interface ITesseractJobDetailService extends IService<TesseractJobDetail> {
    TesseractAdminRegistryResDTO registry(TesseractAdminRegistryRequest tesseractAdminRegistryRequest) throws Exception;
}
