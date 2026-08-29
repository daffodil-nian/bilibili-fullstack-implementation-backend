package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.dto.article.ArticlePublishDto;
import org.springframework.transaction.annotation.Transactional;

public interface IArticleService {
    @Transactional(rollbackFor = Exception.class)
    Long publish(Long uid, ArticlePublishDto dto);
}
