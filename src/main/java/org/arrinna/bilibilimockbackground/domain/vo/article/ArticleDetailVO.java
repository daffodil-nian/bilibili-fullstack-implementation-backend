package org.arrinna.bilibilimockbackground.domain.vo.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// ArticleDetailVO
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/***
 * 专栏详情，不包括评论区的信息
 */
public class ArticleDetailVO {
    //专栏ID
    private Long articleId;
    //专栏标题，封面，概括，内容
    private String title, cover, summary, content;

    //作者ID
    private Long userId;

    //作者昵称，头像
    private String authorNickname, authorAvatar;
    private LocalDateTime publishTime;

    private Long categoryId;
    private String categoryName; // 可选


    private Long collectionId;
    private Integer status;
    private Integer likeCount, commentCount, clickCount, shareCount;

    private List<String> tagNames;

    private Boolean hasLiked;
}