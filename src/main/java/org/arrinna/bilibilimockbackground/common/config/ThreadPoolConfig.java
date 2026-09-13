package org.arrinna.bilibilimockbackground.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 统一管理线程池
 */
public class ThreadPoolConfig implements AsyncConfigurer {

    /**
     * 参考mallchat的写法，整了一个线程池
     */
    public static final String BILI_EXECUTOR="biliExecutor";
    /**
     * websocket通信的线程池
     */
    public static final String WS_EXECUTOR="wsExecutor";

    /**
     * 用户操作线程池
     * 只需要设置池子的处理线程数
     *、 队列的最大容量
     * 、线程的前缀名
     * 、拒绝策略
     * @return
     */
    @Bean(BILI_EXECUTOR)
    @Primary
    public ThreadPoolTaskExecutor biliExecutor(){
        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
        executor.setWaitForTasksToCompleteOnShutdown(true); // 线程池优雅停机
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("bili-Executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean(WS_EXECUTOR)
    public ThreadPoolTaskExecutor WsExecutor(){
        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
        executor.setWaitForTasksToCompleteOnShutdown(true); // 线程池优雅停机
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Ws-Executor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
