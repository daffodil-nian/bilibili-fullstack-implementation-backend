package org.arrinna.bilibilimockbackground;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootTest
@Slf4j
public class TestInterface {


    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Test
    public void thread(){
        threadPoolTaskExecutor.execute(
                ()->{
                    if(1==1){
                        log.warn("你好，再见");
                        System.err.println("我要走了！！");
                        throw new RuntimeException("123");
                    }
                }
        );
    }
}
