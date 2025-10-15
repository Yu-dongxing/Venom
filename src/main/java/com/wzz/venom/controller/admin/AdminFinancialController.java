package com.wzz.venom.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.UserFinancial;
import com.wzz.venom.service.user.UserFinancialService;
import com.wzz.venom.task.FinancialIncomeCalculationTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理后台 - 理财管理接口
 * 模块：理财查询 / 更新 / 删除
 */
@RestController
@RequestMapping("/api/admin/financial")
public class AdminFinancialController {

    @Autowired
    private UserFinancialService userFinancialService;

    /**
     * 查询所有用户理财记录
     *
     * @return 所有记录列表
     */
    @GetMapping("/list")
    public Result<List<UserFinancial>> findAllUserFinancialManagementLists() {
        List<UserFinancial> list = userFinancialService.findAll();
        return Result.success(list);
    }

    /**
     *分页查询所有用户理财记录
     *
     * @param current 当前页码, 默认为 1
     * @param size    每页显示条数, 默认为 10
     * @return 分页结果对象，包含了记录列表、总数、总页数等信息
     */
    @GetMapping("/page")
    public Result<Page<UserFinancial>> findPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<UserFinancial> page = new Page<>(current, size);
        Page<UserFinancial> pageResult = userFinancialService.findAllByPage(page);
        return Result.success(pageResult);
    }

    /**
     * 查询指定用户的理财信息
     *
     * @param user 用户名
     * @return 指定用户的理财记录
     */
    @GetMapping("/user")
    public Result<List<UserFinancial>> searchForDesignatedUserSFinancialInformation(@RequestParam String user) {
        List<UserFinancial> list = userFinancialService.queryTheDesignatedUserSFinancialInformation(user);
        return Result.success(list);
    }

    /**
     * 更新指定用户理财信息
     *
     * @param financialPojo 包含理财记录ID及待更新字段的JSON对象
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result<?> updateDesignatedUserSFinancialInformation(@RequestBody UserFinancial financialPojo) {
        // 前端传来的JSON对象必须包含要更新的记录的 "id"
        if (financialPojo.getId() == null) {
            return Result.error("更新失败，缺少记录ID");
        }
        boolean success = userFinancialService.updateUserFinancialInformation(financialPojo);
        return success ? Result.success("更新成功") : Result.error("更新失败，请检查参数或记录是否存在");
    }

    /**
     * 根据ID删除指定用户理财信息
     *
     * @param id 理财记录的唯一ID
     * @return 操作结果
     */
    @PostMapping("/delete")
    public Result<?> deleteDesignatedUserSFinancialInformation(@RequestParam Long id) {
        boolean success = userFinancialService.deleteFinancialInformationById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败，记录可能不存在");
    }
    @Autowired
    private FinancialIncomeCalculationTask financialIncomeCalculationTask;


    /**
     * 手动触发每日理财收益计算任务
     * <p>
     * 该接口用于管理员手动执行一次全用户的理财收益计算与派发。
     * 调用此接口会立即执行计算收益方法。
     * </p>
     * @return 操作结果
     */
    @PostMapping("/trigger-daily-income-calculation")
    public Result<?> manuallyTriggerDailyIncomeCalculation() {
        try {
            financialIncomeCalculationTask.calculateDailyFinancialIncome();
            return Result.success("手动触发每日收益计算任务已成功执行。");
        } catch (Exception e) {
            return Result.error("任务执行失败，请查看服务器日志获取详细信息。错误: " + e.getMessage());
        }
    }
}