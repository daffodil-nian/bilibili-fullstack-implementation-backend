package org.arrinna.bilibilimockbackground.domain.vo.user;
import lombok.Builder;
import lombok.Data;



@Builder
@Data
public class UserSimpleVO {
    private Long uid;
    private String nickname;
    private String avatar;
    private String signature;
    private Integer level;
    private Boolean followed;     // 我是否已关注对方
}
