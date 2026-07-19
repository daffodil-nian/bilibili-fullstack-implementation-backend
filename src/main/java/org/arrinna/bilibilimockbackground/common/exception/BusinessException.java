package org.arrinna.bilibilimockbackground.common.exception;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID =1L;



    public BusinessException(String message) {
        super(message);
    }
}
