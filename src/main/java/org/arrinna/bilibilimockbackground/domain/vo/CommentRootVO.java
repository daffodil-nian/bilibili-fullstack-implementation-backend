package org.arrinna.bilibilimockbackground.domain.vo;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentRootVO {
    private Long commentId;//评论id，类似于百度中的楼
    private Long articleId;

    //评论者信息
    private Long userId;
    private String nickName;
    private Integer level;
    private String avatar;
    private Boolean isAuthor;//是否为专栏作者，如果是就有一个UP的标签

    //正文
    private String content;
    private Integer likeCount;//点赞数
    private Integer replyCount;//回复该评论数，如果超过两条评论就折叠起来TODO


    private Boolean hasLiked;//是否点赞
    private LocalDateTime createTime;//评论时间
    private List<CommentReplyVO> replyList;//回复列表
}
