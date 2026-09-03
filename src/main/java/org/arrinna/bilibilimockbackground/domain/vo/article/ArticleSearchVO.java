package org.arrinna.bilibilimockbackground.domain.vo.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleSearchVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long articleId;
    /** 专栏ID，对应 ColumnEsDoc.columnId / articles.article_id */
    private Long category;
    private String title;
    private String cover;
    private String summary;

    /** 展示用：只取第一个标签 */
    private String tag;

    private Long userId;
    private String authorNickname;

    private Integer likeCount;
    private Integer commentCount;
    private Integer clickCount;
    private LocalDateTime publishTime;

}
