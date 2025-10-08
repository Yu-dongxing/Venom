package com.wzz.venom.controller.user;

import com.wzz.venom.common.Result;
import com.wzz.venom.domain.dto.UserProductDTO;
import com.wzz.venom.domain.entity.UserProduct;
import com.wzz.venom.service.user.UserProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @Autowired
    private UserProductService userProductService;

    /**
     * 用户提交订单 (购买产品)
     * 使用 @RequestBody 接收 DTO 对象
     *
     * @param productDTO 包含用户、产品、金额等信息的数据传输对象
     * @return Result
     */
    @PostMapping("/submitOrder")
    public Result<?> userSubmitsOrder(@RequestBody UserProductDTO productDTO) {
        // 1. 参数基础校验
        if (!StringUtils.hasText(productDTO.getUser()) || !StringUtils.hasText(productDTO.getProductName())) {
            return Result.error("用户名和产品名称不能为空");
        }
        if (productDTO.getAmount() == null || productDTO.getAmount() <= 0) {
            return Result.error("投入金额必须大于0");
        }

        // 2. 将 DTO 转换为 Entity
        UserProduct newProduct = convertDtoToEntity(productDTO);

        // 3. 调用Service层进行业务处理
        boolean success = userProductService.addUserProducts(newProduct);

        // 4. 根据Service层返回结果，封装统一响应
        if (success) {
            return Result.success("下单成功！");
        } else {
            return Result.error(500, "下单失败，系统内部错误"); // 使用500表示服务端问题
        }
    }

    /**
     * 用户获取持有中的产品列表
     *
     * @param user 用户名 (与DTO中的字段 'user' 对应)
     * @return Result<List<UserProduct>>
     */
    @GetMapping("/hold")
    public Result<List<UserProduct>> usersAcquireAndHoldProducts(@RequestParam String user) {
        // 参数校验
        if (!StringUtils.hasText(user)) {
            return Result.error(400, "用户名不能为空");
        }
        // 调用Service层查询
        List<UserProduct> productList = userProductService.searchForProductsHeldByUsersBasedOnTheirOwnership(user);
        // 返回成功结果
        return Result.success(productList);
    }

    /**
     * 用户获取所有已结束的交易记录
     *
     * @param user 用户名
     * @return Result<List<UserProduct>>
     */
    @GetMapping("/records")
    public Result<List<UserProduct>> userObtainsProductRecords(@RequestParam String user) {
        // 参数校验
        if (!StringUtils.hasText(user)) {
            return Result.error(400, "用户名不能为空");
        }
        // 调用Service层查询
        List<UserProduct> productList = userProductService.searchForEndOfUserTransactionProductsBasedOnUserSearch(user);
        return Result.success(productList);
    }

    /**
     * DTO 到 Entity 的转换工具方法
     * @param dto 数据传输对象
     * @return 数据库实体对象
     */
    private UserProduct convertDtoToEntity(UserProductDTO dto) {
        UserProduct entity = new UserProduct();

        // 字段映射
        entity.setUserName(dto.getUser()); // 注意：DTO中的'user'字段映射到Entity的'userName'
        entity.setProductName(dto.getProductName());
        entity.setProductType(dto.getProductType());
        entity.setAmount(dto.getAmount());
        entity.setInterestRate(dto.getInterestRate());

        // --- 业务逻辑相关的默认值设定 ---

        // 状态：下单时状态应由后端强制设定为 "持有中"，忽略前端传来的status值，防止恶意篡改
        entity.setStatus(0); // 0-持有中

        // 到期时间：DTO中没有此字段，这里根据业务规则生成。
        // 假设所有产品默认期限为1年，实际项目中应根据产品信息动态计算。
        entity.setEndTime(LocalDateTime.now().plusYears(1));

        // 预期收益：后端应根据金额和利率精确计算，防止前端计算错误或恶意提交。
        // 强烈建议使用 BigDecimal 进行金融计算以避免精度丢失！
        if (dto.getAmount() != null && dto.getInterestRate() != null) {
            BigDecimal amountBd = BigDecimal.valueOf(dto.getAmount());
            BigDecimal rateBd = BigDecimal.valueOf(dto.getInterestRate());
            BigDecimal income = amountBd.multiply(rateBd);
            entity.setIncome(income.doubleValue());
        }

        return entity;
    }
}