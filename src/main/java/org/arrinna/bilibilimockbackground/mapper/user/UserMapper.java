package org.arrinna.bilibilimockbackground.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;

/**
 * 用户表mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
