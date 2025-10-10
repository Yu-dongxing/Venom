package com.wzz.venom.controller.user;

import com.wzz.venom.common.Result;
import com.wzz.venom.domain.entity.InterestRateConfig;
import com.wzz.venom.service.config.InterestRateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户 配置管理 - 利率档位设置接口
 */
@RestController
@RequestMapping("/api/user/InterestRateConfig")
public class UserInterestRateConfigController {
    @Autowired
    private InterestRateConfigService interestRateConfigService;


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
     * 查询所有
     */
    @GetMapping("/find/all")
    public Result<?> findAll(){
        List<InterestRateConfig> list = interestRateConfigService.list();
        return Result.success(list);
    }
}
