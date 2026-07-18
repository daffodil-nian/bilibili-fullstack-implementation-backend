package org.arrinna.bilibilimockbackground.common.util;


import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class GenerateUserDefaultNickname {

    private final String PREFIX = "bili_";

    // 1. 定义大小写字母+数字的字符池（共62个字符）
    private static final String CHAR_POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01233456789";
    private static final int POOL_LENGTH = CHAR_POOL.length();

    private static final GenerateUserDefaultNickname INSTANCE = new GenerateUserDefaultNickname();

    // 2. 抓取当前时间的 年、月、日、时、分、秒
    // 包含年份后两位（比如26年），确保跨年也不会重复。总共 12 位数字
    // 示例：2026年4月20日 15点30分25秒 -> 260420153025
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    public static GenerateUserDefaultNickname getInstance() {
        return INSTANCE;
    }

    /**
     * 生成规则：bili_ + 12位年月日时分秒 + 2位随机字母数字
     * 最终后缀总长度：14位
     */
    public synchronized String nextUniqueName() {
        // 1. 拿到 12 位的精准时间戳（含年，防止明年今日撞车）
        String timePart = LocalDateTime.now().format(TIME_FORMATTER);

        // 2. 从字符池中随机抽取 2 个字符作为后缀
        ThreadLocalRandom random = ThreadLocalRandom.current();
        char char1 = CHAR_POOL.charAt(random.nextInt(POOL_LENGTH));
        char char2 = CHAR_POOL.charAt(random.nextInt(POOL_LENGTH));

        // 3. 完美拼装
        return PREFIX + timePart + char1 + char2;
    }
}
