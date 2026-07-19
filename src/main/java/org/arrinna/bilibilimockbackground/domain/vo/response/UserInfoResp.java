package org.arrinna.bilibilimockbackground.domain.vo.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.arrinna.bilibilimockbackground.domain.entity.user.IpInfo;

import java.time.LocalDateTime;
import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserInfoResp {

    //1.鉴权凭证
    private String token;

    //2.用户基本信息
    private UserBaseInfo userInfo;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserBaseInfo{
        private Long UID; //用户的UID
        private String nickname;
        private Integer sex;
        private Date birthDay;
        private Integer activeStatus;
        private String avatar;
        private IpInfo ipInfo;
        private LocalDateTime createTime; // 显示来到B站平台距今约多少天
        //这些是前端要返回的数据信息
        // 至于IP地址的话也返回
    }
}
