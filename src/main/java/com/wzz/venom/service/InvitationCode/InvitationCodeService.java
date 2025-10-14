package com.wzz.venom.service.InvitationCode;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wzz.venom.domain.entity.InvitationCode;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InvitationCodeService extends IService<InvitationCode> {
    @Transactional
        // 保证批量插入的原子性
    List<InvitationCode> generateAndAddCodes(Long generatorUserId, int count, Integer expiryDays);

    @Transactional // 事务保证查询和更新的一致性
    InvitationCode useCode(String code, Long usedByUserId);

    boolean deleteCode(String code);

    boolean updateCodeStatus(String code, Integer status);

    InvitationCode addPermanentCode(Long generatorUserId);
}
