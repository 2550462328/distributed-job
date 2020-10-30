package com.zhanghui.advice;

import com.zhanghui.exception.TesseractException;
import com.zhanghui.pojo.CommonResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ControllerExceptionAdvice {
    @ExceptionHandler(TesseractException.class)
    public CommonResponseVO tesseractExceptionExceptionHandler(TesseractException e) {
        e.printStackTrace();
        log.error(e.getMsg());
        return CommonResponseVO.fail(e.getStatus(), e.getMsg(), null);
    }

    @ExceptionHandler(Exception.class)
    public CommonResponseVO commonExceptionHandler(Exception e) {
        e.printStackTrace();
        log.error(e.getMessage());
        return CommonResponseVO.FAIL;
    }
}
