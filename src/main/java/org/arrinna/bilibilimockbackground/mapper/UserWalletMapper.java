package org.arrinna.bilibilimockbackground.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserWallet;

@Mapper
public interface UserWalletMapper extends BaseMapper<UserWallet> {
}
