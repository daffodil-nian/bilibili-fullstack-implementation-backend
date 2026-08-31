package org.arrinna.bilibilimockbackground.testArticle;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.domain.dto.article.ArticlePublishDto;
import org.arrinna.bilibilimockbackground.domain.vo.ArticleDetailVO;
import org.arrinna.bilibilimockbackground.domain.vo.SearchVO;
import org.arrinna.bilibilimockbackground.domain.vo.request.SearchEsReq;
import org.arrinna.bilibilimockbackground.service.IArticleService;
import org.arrinna.bilibilimockbackground.service.ISearchService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
public class test {

    @Resource
    private IArticleService ArticleService;
    @Resource
    private ISearchService searchService;

/**
 * 测试文章发布功能的方法
 * 该方法创建一个ArticlePublishDto对象并调用ArticleService的publish方法发布文章
 */
@Test
    public void test(){

//        // 使用Builder模式构建文章发布数据传输对象(ArticlePublishDto)
//        ArticlePublishDto dto = ArticlePublishDto.builder()
//                .title("《给阿嬷的情书》：一部草根电影的逆袭密码")
//                .content("<p>这是专栏正文内容，用于单元测试。</p>")
//                // 设置文章内容，使用HTML格式
//                .cover("https://example.com/cover.jpg")
//                // 设置文章封面图片URL
//                .summary("今年五一档，一部没有名导的小成本电影打破流量魔咒……")
//                // 设置文章摘要
//                .status(1)                 // ArticleStatusEnum.PUBLISHED
//                .categoryId(1L)            // 对应分类，如「日常」
//                .collection(0L)            // 无文集
//                .tagNames(List.of("电影", "影评"))
//                .build();
//        long res = ArticleService.publish(4L, dto);
//        log.info("发布专栏成功，专栏ID为：{}", res);
//        //然后查看信息
    ArticleDetailVO articleDetailVO = ArticleService.detail(2L,4L);
    //返回查询结果
    SearchEsReq searchEsReq = new SearchEsReq();
    searchEsReq.setKeyword("阿嬷");
    searchEsReq.setSearchType(6);
    searchEsReq.setSortCode(0);

    SearchVO searchVO = searchService.search(searchEsReq);
    log.info("搜索结果：{}", searchVO);
    log.info("文章详情：{}", articleDetailVO);
    }
}
