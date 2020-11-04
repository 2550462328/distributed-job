package com.zhanghui.controller;


import com.zhanghui.core.dto.TesseractExecutorResponse;
import com.zhanghui.core.dto.TesseractHeartbeatRequest;
import com.zhanghui.service.ITesseractExecutorDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.zhanghui.core.constant.CommonConstant.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author zhanghui
 * @since 2020-10-20
 */
@RestController
@RequestMapping("/tesseract-executor-detail")
public class TesseractExecutorDetailController {

    @Autowired
    private ITesseractExecutorDetailService executorDetailService;

    @RequestMapping(HEARTBEAT_MAPPING_SUFFIX)
    public TesseractExecutorResponse heartBeat(@Valid @RequestBody TesseractHeartbeatRequest heartBeatRequest) {
        executorDetailService.heartBeat(heartBeatRequest);
        return TesseractExecutorResponse.SUCCESS;
    }
}
