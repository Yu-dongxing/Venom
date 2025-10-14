package com.wzz.venom.service.impl.InvitationCode;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wzz.venom.domain.entity.InvitationCode;
import com.wzz.venom.mapper.InvitationCodeMapper;
import com.wzz.venom.service.InvitationCode.InvitationCodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert; // Spring 提供的断言工具，非常方便

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class InvitationCodeServiceImpl extends ServiceImpl<InvitationCodeMapper, InvitationCode> implements InvitationCodeService {

    // 定义邀请码状态常量，增强代码可读性和可维护性
    private static final int STATUS_UNUSED = 0;
    private static final int STATUS_USED = 1;
    private static final int STATUS_EXPIRED = 2;


    /**
     * 生成并添加邀请码
     */
    @Transactional
    @Override
    public List<InvitationCode> generateAndAddCodes(Long generatorUserId, int count, Integer expiryDays) {
        Assert.notNull(generatorUserId, "生成者ID不能为空");
        Assert.isTrue(count > 0, "生成数量必须大于0");

        List<InvitationCode> codes = new ArrayList<>();
        LocalDateTime expiryTime = null;
        if (expiryDays != null && expiryDays > 0) {
            expiryTime = LocalDateTime.now().plusDays(expiryDays);
        }

        for (int i = 0; i < count; i++) {
            InvitationCode invitationCode = new InvitationCode();
            // 使用UUID生成一个简单的、不易重复的邀请码
            invitationCode.setCode(UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
            invitationCode.setGeneratorUserId(generatorUserId);
            invitationCode.setStatus(STATUS_UNUSED);
            invitationCode.setExpiryTime(expiryTime);
            // 这里可以设置继承自 BaseEntity 的 createTime等字段
            invitationCode.setCreateTime(LocalDateTime.now());
            codes.add(invitationCode);
        }

        // 批量保存
        this.saveBatch(codes);

        return codes;
    }

    /**
     * 使用邀请码
     */
    @Transactional
    @Override
    public InvitationCode useCode(String code, Long usedByUserId) {
        Assert.hasText(code, "邀请码不能为空");
        Assert.notNull(usedByUserId, "使用者ID不能为空");

        // 1. 根据code查询邀请码
        LambdaQueryWrapper<InvitationCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InvitationCode::getCode, code);
        InvitationCode invitationCode = this.getOne(queryWrapper);

        // 2. 校验邀请码是否存在
        if (invitationCode == null) {
            throw new RuntimeException("邀请码不存在");
        }

        // 3. 校验邀请码是否已被使用
        if (invitationCode.getStatus() == STATUS_USED) {
            throw new RuntimeException("邀请码已被使用");
        }

        // 4. 校验邀请码是否过期
        if (invitationCode.getExpiryTime() != null && LocalDateTime.now().isAfter(invitationCode.getExpiryTime())) {
            // 如果已过期，顺便更新一下状态
            invitationCode.setStatus(STATUS_EXPIRED);
            this.updateById(invitationCode);
            throw new RuntimeException("邀请码已过期");
        }

        // 5. 校验邀请码状态是否为“未使用”
        if (invitationCode.getStatus() != STATUS_UNUSED) {
            throw new RuntimeException("邀请码状态异常，无法使用");
        }

        // 6. 更新邀请码状态
        invitationCode.setStatus(STATUS_USED);
        invitationCode.setUsedByUserId(usedByUserId);
        invitationCode.setUsedTime(LocalDateTime.now());
        // 这里可以设置继承自 BaseEntity 的 updateTime等字段
        invitationCode.setUpdateTime(LocalDateTime.now());

        //删除该邀请码
        boolean success = this.updateById(invitationCode);
        if (!success) {
            // 极端并发情况下可能更新失败，可以抛出异常让事务回滚
            throw new RuntimeException("使用邀请码失败，请重试");
        }

        return invitationCode;
    }

    /**
     * 删除邀请码
     */
    @Override
    public boolean deleteCode(String code) {
        Assert.hasText(code, "邀请码不能为空");
        LambdaQueryWrapper<InvitationCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InvitationCode::getCode, code);
        return this.remove(queryWrapper);
    }

    /**
     * 更新邀请码状态
     */
    @Override
    public boolean updateCodeStatus(String code, Integer status) {
        Assert.hasText(code, "邀请码不能为空");
        Assert.notNull(status, "状态不能为空");

        // 1. 查找邀请码
        LambdaQueryWrapper<InvitationCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(InvitationCode::getCode, code);
        InvitationCode invitationCode = this.getOne(queryWrapper);

        if (invitationCode == null) {
            throw new RuntimeException("邀请码不存在");
        }

        // 2. 更新状态
        invitationCode.setStatus(status);
        invitationCode.setUpdateTime(LocalDateTime.now());

        return this.updateById(invitationCode);
    }

    /**
     * 实现添加单个永久邀请码的方法
     */
    @Override
    public InvitationCode addPermanentCode(Long generatorUserId) {
        // 1. 校验输入参数
        Assert.notNull(generatorUserId, "生成者ID不能为空");
        // 2. 创建邀请码实体对象
        InvitationCode invitationCode = new InvitationCode();
        // 3. 设置属性，满足您的三点要求
        //    - 要求1: 生成邀请码
        invitationCode.setCode(generateUniqueCode());
        invitationCode.setGeneratorUserId(generatorUserId);
        //    - 要求2: 有效期为永久
        invitationCode.setExpiryTime(null); // expiryTime 为 null 即代表永不过期
        //    - 要求3: 状态为未使用
        invitationCode.setStatus(STATUS_UNUSED);
        // 设置创建时间等公共字段
        invitationCode.setCreateTime(LocalDateTime.now());
        // 4. 调用MyBatis-Plus的save方法将其持久化到数据库
        this.save(invitationCode);
        return invitationCode;
    }

    /**
     * 辅助方法：生成唯一的邀请码字符串
     * 您可以根据业务需求自定义生成规则
     */
    private String generateUniqueCode() {
        // 这里我们继续使用UUID简化版作为示例
        // 在高并发场景下，需要考虑code的唯一性，可以增加重试或更复杂的生成算法
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}