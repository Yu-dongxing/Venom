package com.wzz.venom.controller.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.InterestRateConfig;
import com.wzz.venom.service.config.InterestRateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 配置管理 - 利率档位设置接口
 */
@RestController
@RequestMapping("/api/cs/InterestRateConfig")
public class InterestRateConfigController {

    @Autowired
    private InterestRateConfigService interestRateConfigService;

    /**
     * 新增利率档位
     *
     * @param interestRateConfig 利率档位配置信息，通过请求体中的JSON传递
     * @return Result 响应结果
     */
    @PostMapping("/add")
    public Result<?> add(@RequestBody InterestRateConfig interestRateConfig) {
        // 通常实体类ID是自增的，新增时不需要传递ID
        interestRateConfig.setId(null);
        boolean success = interestRateConfigService.save(interestRateConfig);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 根据ID删除利率档位
     *
     * @param id 要删除的利率档位ID，通过URL路径传递
     * @return Result 响应结果
     */
    @PostMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        boolean success = interestRateConfigService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败，可能ID不存在");
    }

    /**
     * 修改利率档位
     *
     * @param interestRateConfig 待修改的利率档位配置信息，通过请求体中的JSON传递，必须包含ID
     * @return Result 响应结果
     */
    @PostMapping("/update")
    public Result<?> update(@RequestBody InterestRateConfig interestRateConfig) {
        if (interestRateConfig.getId() == null) {
            return Result.error("修改失败，ID不能为空");
        }
        boolean success = interestRateConfigService.updateById(interestRateConfig);
        return success ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 根据ID查询利率档位详情
     *
     * @param id 利率档位ID，通过URL路径传递
     * @return Result<InterestRateConfig> 包含查询结果的响应
     */
    @GetMapping("/find/{id}")
    public Result<InterestRateConfig> getById(@PathVariable Long id) {
        InterestRateConfig config = interestRateConfigService.getById(id);
        if (config != null) {
            return Result.success(config);
        } else {
            return Result.error("查询的数据不存在");
        }
    }

    /**
     * 分页查询利率档位列表
     *
     * @param current 当前页码，默认为1
     * @param size    每页显示条数，默认为10
     * @return Result<IPage<InterestRateConfig>> 包含分页信息的响应结果
     */
    @GetMapping("/page")
    public Result<IPage<InterestRateConfig>> getPageList(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {

        // 1. 创建MyBatis-Plus的分页对象
        Page<InterestRateConfig> page = new Page<>(current, size);
        IPage<InterestRateConfig> pageResult = interestRateConfigService.page(page);

        return Result.success(pageResult);
    }
}