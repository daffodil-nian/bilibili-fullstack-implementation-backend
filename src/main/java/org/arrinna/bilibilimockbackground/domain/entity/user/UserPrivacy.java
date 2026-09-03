package org.arrinna.bilibilimockbackground.domain.entity.user;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Builder
@Data
@TableName("user_privacy")
public class UserPrivacy {
    /**
     * 用户UID，主键，关联 user.u_id
     */
    @TableId
    private Long uId;

    /** 公开收藏 1是0否 */
    private Integer showCollect;
    /** 公开生日/标签 */
    private Integer showBirthdayAndTag;
    /** 公开追漫 */
    private Integer showComic;
    /** 公开追番追剧 */
    private Integer showBangumi;
    /** 公开学校 */
    private Integer showSchoolInfo;
    /** 公开粉丝装扮 */
    private Integer showFansDecorate;
    /** 公开最近投币 */
    private Integer showCoinVideo;
    /** 公开最近游戏 */
    private Integer showGame;
    /** 投稿展示直播回放 */
    private Integer showLiveReplay;
    /** 公开最近点赞 */
    private Integer showLikeVideo;
    /** 公开粉丝勋章 */
    private Integer showFansMedal;
    /** 投稿展示课堂 */
    private Integer showClassVideo;
    /** 公开关注列表 */
    private Integer showFollowList;
    /** 公开粉丝列表 */
    private Integer showFansList;
    /** 投稿展示充电专属 */
    private Integer showChargeVideo;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}