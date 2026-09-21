package org.arrinna.bilibilimockbackground.aspect;


import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.annotation.RedissonLock;
import org.arrinna.bilibilimockbackground.common.service.LockService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
@Order(0)
public class RedissonLockAspect {

    @Autowired
    private LockService lockService;

    @Around("@annotation(org.arrinna.bilibilimockbackground.annotation.RedissonLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();

        RedissonLock redissonLock = method.getAnnotation(RedissonLock.class);

        //2.然后就可以在锁中获取一些关键信息了，并且执行语句！

        String prefix = redissonLock.prefixKey();

        String key = redissonLock.key();

        //3.稍微要处理才可以获取到关键信息，先不管
        return null;
    }

}
