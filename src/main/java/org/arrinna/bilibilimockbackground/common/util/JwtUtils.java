package org.arrinna.bilibilimockbackground.common.util;

import cn.hutool.jwt.JWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class JwtUtils {


    //token密钥
    @Value("${spring.jwt.secret}")
    private String secret;

    private static final String UID_CLAIM ="uid";
    private static final String CREATE_TIME = "create_time";

    /**
     * 建JWT，包括header，payload和signature
     * @param uid
     * @return
     */
    public String createToken(Long uid){
        String token = JWT.create()
                .setPayload(UID_CLAIM, uid)
                .setPayload(CREATE_TIME,new Date())
                .setKey(secret.getBytes())//密钥
                .sign();
        return token;
    }
}
