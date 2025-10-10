package com.wzz.venom.service.impl.config;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzz.venom.domain.entity.InterestRateConfig;
import com.wzz.venom.mapper.InterestRateConfigMapper;
import com.wzz.venom.service.config.InterestRateConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InterestRateConfigServiceImpl extends ServiceImpl<InterestRateConfigMapper, InterestRateConfig> implements InterestRateConfigService{

}
