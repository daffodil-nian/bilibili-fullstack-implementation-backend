package org.arrinna.bilibilimockbackground.testArticle;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.arrinna.bilibilimockbackground.domain.dto.article.ArticlePublishDto;
import org.arrinna.bilibilimockbackground.service.IArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@Slf4j
public class test {

    @Resource
    private IArticleService ArticleService;

/**
 * 测试文章发布功能的方法
 * 该方法创建一个ArticlePublishDto对象并调用ArticleService的publish方法发布文章
 */
@Test
    public void test(){

        // 使用Builder模式构建文章发布数据传输对象(ArticlePublishDto)
        ArticlePublishDto dto = ArticlePublishDto.builder()
                .title("《给阿嬷的情书》：一部草根电影的逆袭密码")
                .content("<p>这是专栏正文内容，用于单元测试。</p>")
                // 设置文章内容，使用HTML格式
                .cover("https://example.com/cover.jpg")
                // 设置文章封面图片URL
                .summary("今年五一档，一部没有名导的小成本电影打破流量魔咒……")
                // 设置文章摘要
                .status(1)                 // ArticleStatusEnum.PUBLISHED
                .categoryId(1L)            // 对应分类，如「日常」
                .collection(0L)            // 无文集
                .tagNames(List.of("电影", "影评"))
                .build();
        long res = ArticleService.publish(4L, dto);
        log.info("发布专栏成功，专栏ID为：{}", res);
    }
}
