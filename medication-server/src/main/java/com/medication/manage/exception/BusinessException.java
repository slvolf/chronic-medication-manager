package com.medication.manage.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 * 用于在 Service 层抛出可预知的业务错误
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
