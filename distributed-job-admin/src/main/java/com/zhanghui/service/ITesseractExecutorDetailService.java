package com.zhanghui.service;

import com.zhanghui.core.dto.TesseractHeartbeatRequest;
import com.zhanghui.entity.TesseractExecutorDetail;
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
public interface ITesseractExecutorDetailService extends IService<TesseractExecutorDetail> {
    List<TesseractExecutorDetail> listInvalid();

    void heartBeat(TesseractHeartbeatRequest heartBeatRequest);
}
