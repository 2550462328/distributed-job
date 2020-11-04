package com.zhanghui.controller;

import com.zhanghui.core.JobClientBootstrap;
import com.zhanghui.core.constant.CommonConstant;
import com.zhanghui.core.dto.TesseractExecutorRequest;
import com.zhanghui.core.dto.TesseractExecutorResponse;
import com.zhanghui.core.executor.JobExecuteDelegator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author: ZhangHui
 * @date: 2020/10/22 17:10
 * @version：1.0
 */
@RestController
@ControllerAdvice
@Slf4j
public class JobNotifyController {

    @RequestMapping(CommonConstant.EXECUTE_MAPPING)
    public TesseractExecutorResponse executeJob(@Valid @RequestBody TesseractExecutorRequest tesseractExecutorRequest) {
        return JobExecuteDelegator.execute(tesseractExecutorRequest);
    }

    @ExceptionHandler(Throwable.class)
    public TesseractExecutorResponse exceptionHandler(Throwable throwable) {
        log.error(throwable.getMessage());
        TesseractExecutorResponse fail = new TesseractExecutorResponse(TesseractExecutorResponse.FAIL_STAUTS, throwable.getMessage());
        return fail;
    }
}
