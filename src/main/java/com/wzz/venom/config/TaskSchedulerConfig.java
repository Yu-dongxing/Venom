package com.wzz.venom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 定时任务调度器配置
 * <p>
 * 用于处理动态的、一次性的定时任务，例如产品到期结算。
 * </p>
 */
@Configuration
public class TaskSchedulerConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // 配置线程池大小，根据业务并发量调整，这里设置为10
        scheduler.setPoolSize(10);
        // 设置线程名前缀，方便日志追踪
        scheduler.setThreadNamePrefix("product-settle-");
        // 设置当调度器shutdown时，是否等待任务执行完毕
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        // 设置等待时长
        scheduler.setAwaitTerminationSeconds(60);
        // 初始化
        scheduler.initialize();
        return scheduler;
    }
}