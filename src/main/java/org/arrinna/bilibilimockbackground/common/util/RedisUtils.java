package org.arrinna.bilibilimockbackground.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisUtils {

    private static StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate template) {
        stringRedisTemplate = template;
    }
    public static String getKey(String key,Object... objects){
        return String.format(key,objects);
    }

    /**
     * 设置key-value
     * @param key
     * @param value
     * @param time   过期时间
     * @param timeUnit  单位
     * @return
     */
    public static Boolean set(String key, Object value, long time, TimeUnit timeUnit){
        try{
            stringRedisTemplate.opsForValue().set(key,value.toString(),time,timeUnit);
            return true;
        }
        catch (Exception e){
            log.error(e.getMessage(),e);
            return false;
        }
    }

    public static Boolean set(String key,Object value){
        try{
            stringRedisTemplate.opsForValue().set(key,value.toString());
            return true;
        }
        catch (Exception e){
            log.error(e.getMessage(),e);
            return false;
        }
    }
    public static String get(String key){
        try{
            return stringRedisTemplate.opsForValue().get(key);
        }
        catch (Exception e){
            return null;
        }
    }

    /**
     * EXPIRE KEY SECONDS 这个是redis的命令
     * @param key
     * @param time
     * @param timeUnit
     * @return
     */
    public static Boolean expire(String key,long time,TimeUnit timeUnit){
        try {
            return stringRedisTemplate.expire(key,time,timeUnit);
        }
        catch (Exception e){
            return false;
        }
    }
    /**
     * 指定缓存失效时间
     * @param key
     * @param time
     * @return
     */
    public static Boolean expire(String key,long time){
        try{
            if(time>0){

            }
        } catch (Exception e) {

            log.error("redis expire error",e);
            return false;
        }
        return true;
    }
    public static Long increment(String key) {
        try {
            return stringRedisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("redis increment error", e);
            return null;
        }
    }

    public static Boolean setIfAbsent(String key, String value) {
        try {
            return Boolean.TRUE.equals(
                    //内置的方法
                    stringRedisTemplate.opsForValue().setIfAbsent(key, value)
            );
        } catch (Exception e) {
            log.error("redis setIfAbsent error", e);
            return false;
        }
    }

}
