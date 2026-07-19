package org.arrinna.bilibilimockbackground.common.util;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;

public class PasswordUtil {

    /**
     * 系统固定的盐（暗号），全系统所有用户共用，永不丢失
     * 建议长度在16位以上，越复杂越好
     */
    private static final String STATIC_SALT = "BilibiliMock_Arrinna_2026_Secret!";

    /**
     * 使用固定盐加密（盐值混淆 + 1024次循环散列）
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptWithStaticSalt(String password) {
        // 1. 改变拼接策略：把密码夹在固定盐的中间
        String hashed = STATIC_SALT.substring(0, 8) + password + STATIC_SALT.substring(8);

        // 2. 循环加密 1024 次
        for (int i = 0; i < 1024; i++) {
            hashed = SecureUtil.md5(hashed);
        }

        return hashed;
    }

    /**
     * 验证固定盐密码
     * @param inputPassword 用户输入的明文密码
     * @param dbHashPassword 数据库里存的密文
     * @return 是否匹配
     */
    public static boolean verifyWithStaticSalt(String inputPassword, String dbHashPassword) {
        // 用同样的固定盐加密后，直接比对
        return encryptWithStaticSalt(inputPassword).equals(dbHashPassword);
    }
}
