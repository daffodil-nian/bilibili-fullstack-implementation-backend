package org.arrinna.bilibilimockbackground.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserFollow;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {
}
