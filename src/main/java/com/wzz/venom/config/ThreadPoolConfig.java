package com.wzz.venom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executors;


/**
 * 线程池配置，启用JDK 21虚拟线程
 */
@Configuration
public class ThreadPoolConfig {

    public static final String VIRTUAL_THREAD_EXECUTOR = "virtualThreadExecutor";

    @Bean(VIRTUAL_THREAD_EXECUTOR)
    public AsyncTaskExecutor asyncTaskExecutor() {
        // JDK 21 newVirtualThreadPerTaskExecutor() 会为每个任务创建一个新的虚拟线程
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
