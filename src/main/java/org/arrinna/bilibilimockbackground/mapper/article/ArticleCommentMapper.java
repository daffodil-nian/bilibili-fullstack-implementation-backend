package org.arrinna.bilibilimockbackground.mapper.article;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.arrinna.bilibilimockbackground.domain.entity.articles.ArticleComment;

@Mapper
public interface ArticleCommentMapper extends BaseMapper<ArticleComment> {
}
