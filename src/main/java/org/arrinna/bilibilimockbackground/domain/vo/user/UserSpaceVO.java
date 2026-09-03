package org.arrinna.bilibilimockbackground.domain.vo.user;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;


@Builder
@Data
public class UserSpaceVO {
    private Long uid;
    private String nickname;
    private String avatar;
    private LocalDate birthday;   // 隐私关闭时返回 null
    private Integer level;
    private String signature;
    private Integer followCount;
    private Integer fansCount;
    private Long likeCount;       // 获赞
    private Long playCount;       // 播放/点击
    private Boolean showFollowList; // 前端是否展示「关注」入口
    private Boolean showFansList;
    private Boolean followed;     // 当前登录用户是否已关注 TA（未登录 null/false）
}
