package org.arrinna.bilibilimockbackground.common.exception;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID =1L;

    private Integer errorCode;

    private String errMsg;

    public BusinessException(String message) {
        super(message);
        this.errMsg=message;

    }
    public BusinessException(Integer errorCode,String message) {
        super(message);
        this.errorCode=errorCode;
        this.errMsg=message;
    }
    public BusinessException(ErrorCodeEnum error) {
        super(error.getMsg());
        this.errorCode=error.getCode();
        this.errMsg=error.getMsg();
    }
}
