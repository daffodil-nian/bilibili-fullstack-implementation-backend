package org.arrinna.bilibilimockbackground.domain.vo.comment;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
/**
 * 二级评论
 */
public class CommentReplyVO {

    private Long articleId;//文章ID
    private Long commentId;//指的是评论Id
    private Long rootCommentId;//根ID,如果在id为2的评论下评论这个id就是2
    private Long toCommentId;//回复的评论ID
    private Long userId;
    private String nickName;
    private String avatar;
    private String content;
    private Integer likeCount;
    private Boolean hasLiked;
    private String createTime;

    /**
     * 1. **commentId**：单条评论本身的编号（这条发言的编号）
     * 2. **rootCommentId**：**本楼最顶部那条一级评论的 commentId**（根评论的发言编号，不是人的 ID）
     * 3. **toCommentId**：我当前这条，直接回复的那一条评论的 commentId
     */
}
