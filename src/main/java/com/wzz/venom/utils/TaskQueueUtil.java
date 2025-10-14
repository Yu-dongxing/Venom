package com.wzz.venom.utils;

import com.wzz.venom.config.ThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * 基于虚拟线程的队列线程工具类
 */
@Component
public class TaskQueueUtil {

    private static final Logger logger = LoggerFactory.getLogger(TaskQueueUtil.class);

    private static AsyncTaskExecutor executor;

    /**
     * 通过构造函数注入线程池
     * @param asyncTaskExecutor 虚拟线程池
     */
    @Autowired
    public TaskQueueUtil(@Qualifier(ThreadPoolConfig.VIRTUAL_THREAD_EXECUTOR) AsyncTaskExecutor asyncTaskExecutor) {
        TaskQueueUtil.executor = asyncTaskExecutor;
    }

    /**
     * 对外提供的唯一方法，用于提交任务到队列
     * @param task 需要执行的任务，使用Runnable函数式接口
     */
    public static void execute(Runnable task) {
        if (executor == null) {
            logger.error("线程池尚未初始化！");
            // 在某些极端情况下（例如，Spring容器还未完全初始化），可以考虑提供一个备用策略
            // throw new IllegalStateException("AsyncTaskExecutor has not been initialized.");
            return;
        }

        try {
            executor.execute(task);
        } catch (Exception e) {
            logger.error("向线程池提交任务时发生错误", e);
        }
    }
}