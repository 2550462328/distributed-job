package com.zhanghui.controller;


import com.zhanghui.core.dto.TesseractAdminRegistryRequest;
import com.zhanghui.core.dto.TesseractAdminRegistryResDTO;
import com.zhanghui.core.dto.TesseractExecutorResponse;
import com.zhanghui.service.ITesseractJobDetailService;
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
@RequestMapping("/tesseract-job-detail")
public class TesseractJobDetailController {
    
    @Autowired
    private ITesseractJobDetailService jobDetailService;

    @RequestMapping(REGISTRY_MAPPING_SUFFIX)
    public TesseractExecutorResponse registry(@Valid @RequestBody TesseractAdminRegistryRequest tesseractAdminRegistryRequest) throws Exception {
        TesseractAdminRegistryResDTO registryResp = jobDetailService.registry(tesseractAdminRegistryRequest);
        TesseractExecutorResponse success = new TesseractExecutorResponse(TesseractExecutorResponse.SUCCESS_STATUS, registryResp);
        return success;
    }
}
