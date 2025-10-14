package com.wzz.venom.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.wzz.venom.common.Result;
import com.wzz.venom.domain.dto.UserProductDTO;
import com.wzz.venom.domain.entity.User;
import com.wzz.venom.domain.entity.UserProduct;
import com.wzz.venom.exception.BusinessException;
import com.wzz.venom.service.user.UserProductService;
import com.wzz.venom.service.user.UserService;
import com.wzz.venom.service.webSocket.WebSocketNotifyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户产品接口控制器
 * 模块：产品下单 / 产品查询 / 用户持有产品
 * (面向C端用户)
 */
@RestController
@RequestMapping("/api/user/product")
public class UserProductController {

    private static final Logger log = LogManager.getLogger(UserProductController.class);
    @Autowired
    private UserProductService userProductService;

    @Autowired
    private WebSocketNotifyService webSocketNotifyService;

    @Autowired
    private UserService userService;

    /**
     * 用户提交订单 (购买产品)
     * @param productDTO 包含用户、产品、金额等信息的数据传输对象
     * @return Result
     */
    @PostMapping("/submitOrder")
    public Result<?> userSubmitsOrder(@RequestBody UserProductDTO productDTO) {
        productDTO.setCycleType("s");
        if (!StringUtils.hasText(productDTO.getProductName())) {
            return Result.error("产品名称不能为空");
        }
        if (productDTO.getAmount() == null || productDTO.getAmount() <= 0) {
            return Result.error("投入金额必须大于0");
        }

        try {
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userService.queryUserByUserId(userId);

            UserProduct newProduct = convertDtoToEntity(productDTO);
            newProduct.setUserName(user.getUserName());

            // 调用已经整合了扣款和下单逻辑的服务方法
            boolean success = userProductService.addUserProducts(newProduct);

            if (success) {
                webSocketNotifyService.sendUserPurchaseNotification(
                        user.getUserName(),
                        productDTO.getProductName(),
                        productDTO.getAmount() // 使用实际金额
                );
                return Result.success("下单成功！");
            } else {
                return Result.error("下单失败，请稍后重试");
            }
        } catch (BusinessException e) {
            log.warn("下单业务异常: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 捕获其他所有未预料到的异常
            log.error("用户下单时发生未知异常", e);
            return Result.error("系统繁忙，请稍后再试");
        }
    }

    /**
     * 用户获取持有中的产品列表
     *
     * @return Result<List<UserProduct>>
     */
    @GetMapping("/hold")
    public Result<?> usersAcquireAndHoldProducts() {
        try{
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userService.queryUserByUserId(userId);
            List<UserProduct> productList = userProductService.searchForProductsHeldByUsersBasedOnTheirOwnership(user.getUserName());
            return Result.success(productList);
        }
        catch (BusinessException e){
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户获取所有已结束的交易记录
     *
     * @return Result<List<UserProduct>>
     */
    @GetMapping("/records")
    public Result<List<UserProduct>> userObtainsProductRecords() {
        try{
            StpUtil.checkLogin();
            Long userId = StpUtil.getLoginIdAsLong();
            User user = userService.queryUserByUserId(userId);
            // 调用Service层查询
            List<UserProduct> productList = userProductService.searchForEndOfUserTransactionProductsBasedOnUserSearch(user.getUserName());
            return Result.success(productList);
        }
        catch (BusinessException e){
            return Result.error(e.getMessage());
        }


    }

    /**
     * DTO 到 Entity 的转换工具方法 (修改后)
     *
     * @param dto 数据传输对象
     * @return 数据库实体对象
     */
    private UserProduct convertDtoToEntity(UserProductDTO dto) {
        UserProduct entity = new UserProduct();

        // --- 基础字段映射 ---
        entity.setUserName(dto.getUser()); // 注意：DTO中的'user'字段映射到Entity的'userName'
        entity.setProductName(dto.getProductName());
        entity.setProductType(dto.getProductType());
        entity.setAmount(dto.getAmount());
        entity.setInterestRate(dto.getInterestRate());
        entity.setCycleType(dto.getCycleType());   // 映射周期类型
        entity.setCycleValue(dto.getCycleValue()); // 映射周期值
        entity.setIncomeStatus(dto.getIncomeStatus()); // 映射收益状态
        entity.setValue(dto.isValue()); //
        try {
            Duration duration = calculateDuration(dto.getCycleType(), dto.getCycleValue());
            entity.setEndTime(LocalDateTime.now().plus(duration));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无效的周期数值: " + dto.getCycleValue());
        } catch (IllegalArgumentException e) {
             log.error("不支持的周期类型: {}", dto.getCycleType());
            throw e;
        }

        // 2. 移除预期收益的计算逻辑
        // entity.setIncome(null); // income 字段默认为 null，无需显式设置

        // 3. 设置初始状态
        entity.setStatus(0); // 0-持有中

        return entity;
    }

    /**
     * 根据周期类型和周期值计算持续时间 (Duration)
     *
     * @param cycleType  周期类型 (例如: "s"-秒, "m"-分钟, "h"-小时, "d"-天)
     * @param cycleValue 周期数值 (字符串形式的数字)
     * @return Java 8 的 Duration 对象
     * @throws NumberFormatException      如果 cycleValue 不是有效的数字
     * @throws IllegalArgumentException   如果 cycleType 是不被支持的类型
     */
    private Duration calculateDuration(String cycleType, String cycleValue) {
        // 将周期值字符串转换为 long 类型
        long value = Long.parseLong(cycleValue);

        // 使用 Java 17 的增强型 switch 表达式判断周期类型
        return switch (cycleType.toLowerCase()) { // 转换为小写以兼容大小写
            case "s" -> Duration.ofSeconds(value);
            case "m" -> Duration.ofMinutes(value);
            case "h" -> Duration.ofHours(value);
            case "d" -> Duration.ofDays(value);
            // 可以根据需要扩展更多类型，如 "w" (周), "M" (月) 等
            // case "w" -> Duration.ofDays(value * 7);
            default -> throw new IllegalArgumentException("不支持的周期类型: " + cycleType);
        };
    }
}