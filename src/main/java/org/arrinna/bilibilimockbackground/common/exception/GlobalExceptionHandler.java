package org.arrinna.bilibilimockbackground.common.exception;


import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    public Result<?> businessExceptionHandler(BusinessException e){
        log.info("业务异常:{}",e.getMessage());
        return Result.fail(e.getErrorCode(),e.getErrMsg());
    }
}
