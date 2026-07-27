package org.arrinna.bilibilimockbackground.domain.vo;

import lombok.Builder;
import lombok.Data;
import org.arrinna.bilibilimockbackground.domain.entity.user.IpInfo;
import org.arrinna.bilibilimockbackground.domain.entity.user.UserLvInfo;
import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;

import java.io.Serializable;
import java.util.Date;

@Builder
@Data
public class UserVO implements Serializable {
    private Long UID; //用户的UID
    private String nickname;
    private Integer sex;
    private Date birthDay;
    private Integer activeStatus;
    private String avatar;
    private IpInfo ipInfo;
    private String signature; // 用户签名
    private UserLvInfo userLvInfo;
    private UserInfoResp.UserFollowResp userFollowInfo;
}
