package com.medication.manage.exception;

import com.medication.manage.vo.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理基础类
 * 统一捕获业务异常和系统异常，返回标准 Result 格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public Result<?> handleBindException(org.springframework.validation.BindException e) {
        String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return Result.error(400, msg);
    }

    /**
     * 处理权限异常
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Result<?> handleAccessDeniedException() {
        return Result.error(403, "无权限访问");
    }

    /**
     * 处理其他未知异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        e.printStackTrace();
        return Result.error("服务器内部错误：" + e.getMessage());
    }
}
