package com.wzz.venom.service.impl.product;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wzz.venom.domain.entity.UserProduct;
import com.wzz.venom.service.product.ProductSchedulingService;
import com.wzz.venom.service.product.ProductSettlementService;
import com.wzz.venom.service.user.UserProductService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 产品结算任务调度服务实现类
 */
@Service
public class ProductSchedulingServiceImpl implements ProductSchedulingService {

    private static final Logger log = LoggerFactory.getLogger(ProductSchedulingServiceImpl.class);

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private ProductSettlementService productSettlementService;

    @Autowired
    private UserProductService userProductService;

    @Override
    public void scheduleProductSettlement(UserProduct product) {
        if (product.getId() == null || product.getEndTime() == null) {
            log.error("调度任务失败：产品ID或结束时间为空。Product: {}", product);
            return;
        }

        // 获取产品的到期时间
        LocalDateTime endTime = product.getEndTime();

        // 如果到期时间已过，则不再安排任务
        if (endTime.isBefore(LocalDateTime.now())) {
            log.warn("产品ID: {} 的到期时间已过，不再安排调度任务。", product.getId());
            return;
        }

        // 创建一个Runnable任务，任务内容就是调用结算服务
        Runnable task = () -> productSettlementService.settleProduct(product.getId());

        // 将LocalDateTime转换为Instant以供TaskScheduler使用
        Instant instant = endTime.atZone(ZoneId.systemDefault()).toInstant();

        // 提交任务到调度器
        taskScheduler.schedule(task, instant);

        log.info("已成功安排产品ID: {} 的结算任务，预计执行时间: {}", product.getId(), endTime);
    }

    /**
     * 在服务启动后，自动检查并重新调度未完成的任务
     * 这是为了防止服务重启导致内存中的定时任务丢失
     */
    //todo
//    @PostConstruct
    public void reschedulePendingTasksOnStartup() {
        log.info("服务启动，开始重新调度未完成的产品结算任务...");
        QueryWrapper<UserProduct> queryWrapper = new QueryWrapper<>();
        // 查询所有"持有中"的产品
        queryWrapper.eq("status", 0);
        List<UserProduct> pendingProducts = userProductService.list(queryWrapper);

        if (pendingProducts.isEmpty()) {
            log.info("没有需要重新调度的任务。");
            return;
        }

        int rescheduledCount = 0;
        for (UserProduct product : pendingProducts) {
            // 检查到期时间，如果服务停机期间已经到期，立即结算
            if (product.getEndTime().isBefore(LocalDateTime.now())) {
                log.warn("产品ID: {} 在服务离线期间已到期，将立即执行结算。", product.getId());
                // 在新线程中执行，避免阻塞主启动流程
                new Thread(() -> productSettlementService.settleProduct(product.getId())).start();
                rescheduledCount++;
            } else {
                // 如果尚未到期，重新安排定时任务
                scheduleProductSettlement(product);
                rescheduledCount++;
            }
        }
        log.info("任务重新调度完成，共处理 {} 个任务。", rescheduledCount);
    }
}