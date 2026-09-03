package org.arrinna.bilibilimockbackground.domain.vo.request;

import lombok.Data;

/**
 * todo
 * 完善公开关注列表和粉丝列表
 */
@Data
public class UserPrivacyReq {
    /** 公开我的收藏 */
    private Boolean showCollect;
    /** 公开生日、个人标签 */
    private Boolean showBirthdayAndTag;
    /** 公开追漫(仅APP) */
    private Boolean showComic;
    /** 公开追番追剧 */
    private Boolean showBangumi;
    /** 公开学校信息 */
    private Boolean showSchoolInfo;
    /** 公开粉丝装扮(仅APP) */
    private Boolean showFansDecorate;
    /** 公开最近投币视频 */
    private Boolean showCoinVideo;
    /** 公开最近玩过的游戏 */
    private Boolean showGame;
    /** 投稿列表展示直播回放 */
    private Boolean showLiveReplay;
    /** 公开最近点赞视频 */
    private Boolean showLikeVideo;
    /** 公开佩戴粉丝勋章 */
    private Boolean showFansMedal;
    /** 投稿列表展示课堂视频 */
    private Boolean showClassVideo;
    /** 公开我的关注列表 */
    private Boolean showFollowList;
    /** 公开我的粉丝列表 */
    private Boolean showFansList;
    /** 投稿列表展示包月充电专属视频 */
    private Boolean showChargeVideo;
}
