package org.arrinna.bilibilimockbackground.domain.vo.response;
import lombok.Data;
import org.arrinna.bilibilimockbackground.domain.entity.user.IpInfo;
import java.util.Date;

@Data
public class UserInfoResp {
    private Long UID; //用户的UID
    private String nickname;
    private Integer sex;
    private Date birthDay;
    private String avatar;
    private IpInfo ipInfo;
    //这些是前端要返回的数据信息，至于IP地址的话也返回   nmjhjmk n,k k, mj ,mkjn   n mn,jk mn,mn mmm
}
