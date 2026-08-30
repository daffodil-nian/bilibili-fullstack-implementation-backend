package org.arrinna.bilibilimockbackground.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import org.arrinna.bilibilimockbackground.common.constant.RedisKey;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;
import org.arrinna.bilibilimockbackground.common.util.AssertUtil;
import org.arrinna.bilibilimockbackground.common.util.RedisUtils;
import org.arrinna.bilibilimockbackground.domain.dto.article.ArticleListQuery;
import org.arrinna.bilibilimockbackground.domain.dto.article.ArticlePublishDto;
import org.arrinna.bilibilimockbackground.domain.entity.articles.Article;
import org.arrinna.bilibilimockbackground.domain.entity.articles.ArticleTag;
import org.arrinna.bilibilimockbackground.domain.entity.articles.ArticleTagRelation;
import org.arrinna.bilibilimockbackground.domain.entity.articles.ArticleThumb;
import org.arrinna.bilibilimockbackground.domain.entity.user.User;
import org.arrinna.bilibilimockbackground.domain.enums.ArticleStatusEnum;
import org.arrinna.bilibilimockbackground.domain.esdoc.ColumnEsDoc;
import org.arrinna.bilibilimockbackground.domain.vo.ArticleDetailVO;
import org.arrinna.bilibilimockbackground.domain.vo.ArticleListVO;
import org.arrinna.bilibilimockbackground.esdao.article.ColumnEsDao;
import org.arrinna.bilibilimockbackground.mapper.article.ArticleMapper;
import org.arrinna.bilibilimockbackground.mapper.article.ArticleTagMapper;
import org.arrinna.bilibilimockbackground.mapper.article.ArticleTagRelationMapper;
import org.arrinna.bilibilimockbackground.mapper.article.ArticleThumbMapper;
import org.arrinna.bilibilimockbackground.mapper.user.UserMapper;
import org.arrinna.bilibilimockbackground.service.IArticleService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class ArticleServiceImpl implements IArticleService {

    //1.发布专栏，返回专栏ID

    @Resource
    private ArticleMapper articleMapper;
    @Resource
    private ArticleTagMapper articleTagMapper;
    @Resource
    private ArticleTagRelationMapper articleTagRelationMapper;
    @Resource
    private ArticleThumbMapper articleThumbMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ColumnEsDao columnEsDao;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long publish(Long uid, ArticlePublishDto dto){
        //1.发布专栏要调用数据库,如果参数不符合要求就抛出异常
        AssertUtil.isFalse(!StringUtils.hasText(dto.getTitle()), ErrorCodeEnum.ARTICLE_TITLE_EMPTY);
        AssertUtil.isFalse(!StringUtils.hasText(dto.getContent()), ErrorCodeEnum.ARTICLE_CONTENT_EMPTY);
        AssertUtil.isFalse(!StringUtils.hasText(dto.getCover()), ErrorCodeEnum.ARTICLE_COVER_EMPTY);
        AssertUtil.isFalse(!StringUtils.hasText(dto.getSummary()), ErrorCodeEnum.ARTICLE_SUMMARY_EMPTY);
        //2.赋值
        int status = dto.getStatus()==null?
                ArticleStatusEnum.PUBLISHED.getCode():dto.getStatus();
        Article article= Article.builder()
                .title(dto.getTitle())
                .cover(dto.getCover())
                .content(dto.getContent())
                .summary(dto.getSummary())
                .status(status)
                .userId(uid)
                .categoryId(dto.getCategoryId())
                .collectionId(dto.getCollection())
//                .createTime(LocalDateTime.now())
                .commentCount(0)
                .likeCount(0)
//                .updateTime(LocalDateTime.now())
                .build();

        //1.如果状态是公开或者私密时间就创建现在的时间
        if(Objects.equals(status,ArticleStatusEnum.PUBLISHED.getCode())||
                Objects.equals(status,ArticleStatusEnum.PRIVATE.getCode())){
            article.setPublishTime(LocalDateTime.now());
            article.setCreateTime(LocalDateTime.now());
            article.setUpdateTime(LocalDateTime.now());
        }
        //2.接下来插入到数据库中
        articleMapper.insert(article);
        //3.接下来插入标签
        saveTags(article.getArticleId(),dto.getTagNames());
        //4.实现es数据库增量新增
        if(Objects.equals(status,ArticleStatusEnum.PUBLISHED.getCode())){
            syncToEs(article,dto.getTagNames(),uid);
        }
        //5.接下来把标签的ID返回回去

        return article.getArticleId();
    }

    private void saveTags(Long articleId, List<String> tagNames) {
        if(CollectionUtils.isEmpty(tagNames)){
            return;
        }
        //2.接下来就要处理标签了,首先去重
        List<String> cleanNames = tagNames.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .distinct()
                .toList();
        if (CollectionUtils.isEmpty(cleanNames)) {
            return;
        }
        for (String tagName :cleanNames) {
            if(!StringUtils.hasText(tagName)){
                continue;
            }
            String tag=tagName.trim();
            ArticleTag articleTag = articleTagMapper.selectOne(
                    new QueryWrapper<ArticleTag>().eq("name", tag)
            );
            //如果没有拿到tag，就new一个
            if(articleTag==null){
                articleTag=new ArticleTag();
                articleTag.setName(tag);
                articleTagMapper.insert(articleTag);
            }
            //拿到tag之后在关联表里面插入新的数据
            ArticleTagRelation relation=new ArticleTagRelation();
            relation.setArticleId(articleId);
            relation.setTagId(articleTag.getId());
            relation.setCreateTime(LocalDateTime.now());
            articleTagRelationMapper.insert(relation);
        }
    }

    private void syncToEs(Article article,
                          List<String> tagNames,Long uid){
        User user=userMapper.selectById(uid);
        ColumnEsDoc doc=ColumnEsDoc.builder()
                .authorNickname(user.getNickname())
                .userId(uid)
                .cover(article.getCover())
                .columnId(article.getArticleId())
                .tag(tagNames.get(0))
                .title(article.getTitle())
                .status(article.getStatus())
                .summary(article.getSummary())
                .publishTime(article.getPublishTime()==null?LocalDateTime.now():article.getPublishTime())
                .updateTime(article.getUpdateTime()==null?LocalDateTime.now():article.getUpdateTime())
                .createTime(article.getCreateTime()==null?LocalDateTime.now():article.getCreateTime())
                .clickCount(article.getClickCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .build();
        columnEsDao.save(doc);

    }

    //TODO等单元测试完上面的方法后就继续完善下面的写法！！！
    public Page<ArticleListVO> list(ArticleListQuery query){

        return null;
    }

    /**
     * 查看文章（专栏）
     * @param articleId
     * @param viewerUid
     * @return
     */
    @Override
    public ArticleDetailVO detail(Long articleId, Long viewerUid){
        Article article = articleMapper.selectById(articleId);
        AssertUtil.isFalse(article==null,ErrorCodeEnum.ARTICLE_NOT_FOUND);

        Integer status=article.getStatus();
        //1.判断用户是否是该文章的作者
        boolean isOwner =viewerUid!=null && viewerUid.equals(article.getUserId());
        boolean publicOk = Objects.equals(status, ArticleStatusEnum.PUBLISHED.getCode());
        AssertUtil.isFalse(!(publicOk || isOwner), ErrorCodeEnum.ARTICLE_FORBIDDEN);

        //2.新增点击量
        int clickCount = article.getClickCount()==null?0:article.getClickCount();
        clickCount=incrArticleClick(articleId,clickCount);

        //3.作者
        User author = userMapper.selectById(article.getUserId());

        //4.标签
        List<String> tagNames = listTagNames(articleId);

        //5.是否已经点赞了,查表判断是否已经点赞了
        boolean hasLiked = false;
        if (viewerUid!=null){
            hasLiked= articleThumbMapper.selectCount(
                    new LambdaQueryWrapper<ArticleThumb>()
                            .eq(ArticleThumb::getArticleId, articleId)
                            .eq(ArticleThumb::getUserId, viewerUid)
            )>0?Boolean.TRUE:Boolean.FALSE;
        }
        //最后返回结果

        return ArticleDetailVO
                .builder()
                .authorAvatar(author.getAvatar())
                .articleId(articleId)
                .authorNickname(author.getNickname())
                .tagNames(tagNames)
                .clickCount(clickCount)
                .cover(article.getCover())
                .content(article.getContent())
                .hasLiked(hasLiked)
                .userId(article.getUserId())
                .title(article.getTitle())
                .commentCount(article.getCommentCount())
                .summary(article.getSummary())
                .shareCount(article.getShareCount())
                .likeCount(article.getLikeCount())
                .collectionId(article.getCollectionId())
                .categoryId(article.getCategoryId())
                .publishTime(article.getPublishTime())
                .status(article.getStatus())
                .build();
    }

    private int incrArticleClick(Long articleId, int dbClickCount) {
        //1.首先根据articleId获取key
        String key = RedisUtils.getKey(RedisKey.ARTICLE_CLICK, articleId);
        //2.判断key是否存在
        if (RedisUtils.get(key) == null) {
            RedisUtils.setIfAbsent(key, String.valueOf(dbClickCount));
        }
        //3.获取key的值，并自增
        Long after = RedisUtils.increment(key);
        return after == null ? dbClickCount : after.intValue();
    }

    private List<String> listTagNames(Long articleId){
        List<ArticleTagRelation> articleTagRelation=articleTagRelationMapper.selectList(
                new LambdaQueryWrapper<ArticleTagRelation>()
                        .eq(ArticleTagRelation::getArticleId,articleId)
        );
        if(CollectionUtils.isEmpty(articleTagRelation)){
            return Collections.emptyList();
        }
        //接下来
        List<Long> tagIds = articleTagRelation
                .stream()
                .map(ArticleTagRelation::getTagId)
                .distinct()
                .toList();
        //根据ID匹配name
        List<ArticleTag> tags=articleTagMapper.selectBatchIds(tagIds);

        List<String> nameList=tags.stream()
                .map(ArticleTag::getName)
                .toList();
        return nameList;
    }

}
