package com.wzz.venom.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private int code;
    private Object data;
    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }
    public BusinessException(int code, String msg, Object data) {
        super(msg);
        this.code = code;
        this.data = data;
    }
}
