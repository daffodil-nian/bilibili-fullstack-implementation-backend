package org.arrinna.bilibilimockbackground.domain.dto;

import lombok.Data;

/**
 * 注册只需要这三个参数就够了
 */
@Data
public class RegisterDto {
    private String username;
    private String password;
    private String checkPassword;
}
