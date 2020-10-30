package com.zhanghui.controller;


import com.zhanghui.core.dto.TesseractAdminJobNotify;
import com.zhanghui.core.dto.TesseractExecutorResponse;
import com.zhanghui.service.ITesseractLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
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
@RequestMapping("/tesseract-log")
public class TesseractLogController {

    @Autowired
    public ITesseractLogService logService;

    @RequestMapping(NOTIFY_MAPPING_SUFFIX)
    private TesseractExecutorResponse notify(@Validated @RequestBody TesseractAdminJobNotify tesseractAdminJobNotify) {
        if(tesseractAdminJobNotify.getLogId() != null) {
            logService.notify(tesseractAdminJobNotify);
        }
        return TesseractExecutorResponse.SUCCESS;
    }
}
