package org.arrinna.bilibilimockbackground.service;

import org.arrinna.bilibilimockbackground.domain.dto.article.ArticlePublishDto;
import org.arrinna.bilibilimockbackground.domain.vo.ArticleDetailVO;
import org.springframework.transaction.annotation.Transactional;

public interface IArticleService {
    @Transactional(rollbackFor = Exception.class)
    Long publish(Long uid, ArticlePublishDto dto);

    ArticleDetailVO detail(Long articleId, Long viewerUid);
}
