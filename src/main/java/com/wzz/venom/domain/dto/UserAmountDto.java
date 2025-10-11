package com.wzz.venom.domain.dto;

// 可以放在你的 dto 或 model 包下
// Lombok 注解可以简化代码，如果没有使用也可以手动添加 getter/setter
import lombok.Data;

@Data
public class UserAmountDto {
    private String user;
    private Double amount;
}