package org.arrinna.bilibilimockbackground.job.once;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.domain.entity.articles.Article;
import org.arrinna.bilibilimockbackground.domain.entity.articles.ArticleTag;
import org.arrinna.bilibilimockbackground.domain.entity.articles.ArticleTagRelation;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.esdoc.ColumnEsDoc;
import org.arrinna.bilibilimockbackground.esdao.article.ColumnEsDao;
import org.arrinna.bilibilimockbackground.mapper.article.ArticleMapper;
import org.arrinna.bilibilimockbackground.mapper.article.ArticleTagMapper;
import org.arrinna.bilibilimockbackground.mapper.article.ArticleTagRelationMapper;
import org.arrinna.bilibilimockbackground.mapper.user.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component

public class FullSyncColumnToEs implements CommandLineRunner {

    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ArticleTagRelationMapper articleTagRelationMapper;
    @Resource
    private ArticleTagMapper articleTagMapper;
    @Resource
    private ColumnEsDao columnEsDao;

    @Value("${cos.client.host}")
    private String host;

    @Override
    public void run(String... args) throws Exception {
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<>());

        List<ColumnEsDoc> columnEsDocs = new ArrayList<>();

        for(Article article:articles){
            columnEsDocs.add(toDoc(article));
        }
        int batch = 500;
        for (int i = 0; i < columnEsDocs.size(); i += batch) {
            int end = Math.min(i + batch, columnEsDocs.size());
            columnEsDao.saveAll(columnEsDocs.subList(i, end));
            log.info("FullSyncArticleToEs {} - {}", i, end);
        }
        log.info("FullSyncArticleToEs done, total={}", columnEsDocs.size());


    }

    private ColumnEsDoc toDoc(Article article){

        //获取用户数据信息！
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().
                eq(User::getUId,article.getUserId()));

       List<String> tagNames = listTagNames(article.getArticleId());
       String tagName = tagNames.isEmpty() ? "默认":tagNames.get(0);

        ColumnEsDoc doc = ColumnEsDoc
                .builder()
                .columnId(article.getArticleId())
                .userId(article.getUserId())
                .title(article.getTitle())
                .cover(host+article.getCover())
                .authorNickname(user.getNickname())
                .tag(tagName==null?" ":tagName)
                .status(article.getStatus())
                .updateTime(article.getUpdateTime())
                .createTime(article.getCreateTime())
                .publishTime(article.getPublishTime())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .clickCount(article.getClickCount())
                .summary(article.getSummary())
                .build();

        return doc;
    }

    private List<String> listTagNames(Long articleId){
        //可以跟据articleId和tagId联动获取names
        List<ArticleTagRelation> relations = articleTagRelationMapper.selectList(
                new LambdaQueryWrapper<ArticleTagRelation>()
                        .eq(ArticleTagRelation::getArticleId,articleId)
        );

        //然后跟据获取到的tag再获取Name
        if(CollectionUtils.isEmpty(relations)){
            return Collections.emptyList();
        }
        List<Long> tagIds = relations.stream()
                .map(ArticleTagRelation::getTagId)
                .distinct()
                .toList();

        //接下来跟据这个获取names

        List<ArticleTag> tags = articleTagMapper.selectBatchIds(tagIds);

        return tags.stream().map(ArticleTag::getName).toList();
    }
}
