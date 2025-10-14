package com.wzz.venom.service.impl.product;

import com.wzz.venom.domain.entity.UserProduct;
import com.wzz.venom.enums.ProductIncomeStatusEnum;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.product.ProductSettlementService;
import com.wzz.venom.service.user.UserFundFlowService;
import com.wzz.venom.service.user.UserProductService;
import com.wzz.venom.service.webSocket.WebSocketNotifyService;
import com.wzz.venom.utils.FinancialCalculatorUtils;
import com.wzz.venom.utils.TaskQueueUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 产品到期结算服务实现类
 */
@Service
public class ProductSettlementServiceImpl implements ProductSettlementService {

    private static final Logger log = LoggerFactory.getLogger(ProductSettlementServiceImpl.class);

    @Autowired
    private UserProductService userProductService;

    @Autowired
    private UserFundFlowService userFundFlowService;

    @Autowired
    private WebSocketNotifyService webSocketNotifyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void settleProduct(Long userProductId) {
        log.info("开始结算产品，ID: {}", userProductId);
        UserProduct product = userProductService.getById(userProductId);
        // 2. 健壮性检查
        if (Objects.isNull(product)) {
            log.warn("结算任务执行失败：未找到ID为 {} 的产品。", userProductId);
            return;
        }
        if (product.getStatus() != 0) {
            log.warn("产品 ID: {} 状态不为 '持有中'，可能已被处理，跳过结算。", userProductId);
            return;
        }

        // 3. 调用工具类计算最终金额
        BigDecimal finalAmount = FinancialCalculatorUtils.calculateFinalAmount(
                product.getIncomeStatus(),
                product.getAmount(),
                product.getInterestRate()
        );

        log.info("产品ID: {}, 用户: {}, 本金: {}, 利率: {}, 收益状态: {}, 结算金额: {}",
                product.getId(), product.getUserName(), product.getAmount(),
                product.getInterestRate(), product.getIncomeStatus(), finalAmount);



        // 提交一个任务到队列中，使用Lambda表达式
        TaskQueueUtil.execute(() -> {
            try {
                String description = String.format("产品 '%s' 到期结算回款", product.getProductName());
                // 注意：increaseUserTransactionAmount接收的是Double，这里做转换
                boolean flowAdded = userFundFlowService.increaseUserTransactionAmount(
                        product.getUserName(),
                        finalAmount.doubleValue(),
                        description
                );
                if (!flowAdded) {
                    // 如果流水增加失败，抛出异常，整个事务回滚
                    throw new RuntimeException("为用户 " + product.getUserName() + " 增加资金流水失败！");
                }
            } catch (BusinessException e) {
                Thread.currentThread().interrupt();
                log.error("任务被中断", e);
            }
        });



        // 5. 更新产品状态为 "已结束" (status=1)
        product.setStatus(1);
        boolean productUpdated = userProductService.updateUserProducts(product);
        if (!productUpdated) {
            throw new RuntimeException("更新产品 " + product.getId() + " 状态失败！");
        }

        // 6. （可选）通过WebSocket通知用户
        String notifyMessage = String.format("您的产品 '%s' 已到期结算，金额 %s 已存入您的账户。", product.getProductName(), finalAmount);
//        webSocketNotifyService.sendToUser(product.getUserName(), notifyMessage);


        log.info("产品ID: {} 结算成功！", userProductId);
    }
}