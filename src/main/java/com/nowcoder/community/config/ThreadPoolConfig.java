package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor () {
//        ThreadPoolExecutor executor = new ThreadPoolExecutor(
//                32, 50, 60, TimeUnit.SECONDS,
//                new ArrayBlockingQueue<>(100),
//                Executors.defaultThreadFactory(),
//                new ThreadPoolExecutor.AbortPolicy()
//        );
//        executor.prestartCoreThread();
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(32);
        taskExecutor.setMaxPoolSize(50);
        taskExecutor.setQueueCapacity(1024);
        taskExecutor.setKeepAliveSeconds(60);
        taskExecutor.setThreadNamePrefix("messageExecutor--");
        // 线程池优雅停机（最多等待60s）
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(60);
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        return taskExecutor;
    }
}
