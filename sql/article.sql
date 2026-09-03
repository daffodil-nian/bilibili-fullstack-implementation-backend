
#
CREATE TABLE IF NOT EXISTS `articles` (
                                          `article_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '专栏ID (即 cv 号)',
                                          `user_id` bigint(20) NOT NULL COMMENT '作者用户ID',
                                          `title` varchar(255) NOT NULL COMMENT '专栏标题',
                                          `cover` varchar(512) DEFAULT '' COMMENT '封面图 URL',
                                          `summary` varchar(512) DEFAULT '' COMMENT '专栏摘要/简介',
                                          `content` longtext NOT NULL COMMENT '专栏正文 (HTML/Markdown)',

    -- 外键与关联 ID
                                          `category_id` int(11) NOT NULL COMMENT '分类ID (关联 article_category.id)',
                                          `collection_id` bigint(20) DEFAULT '0' COMMENT '所属文集ID (关联 article_collection.id)',

    -- 状态与隐私权限
                                          `status` tinyint(4) DEFAULT '1' COMMENT '发布状态: 0-草稿, 1-已发布, 2-审核中, 3-已下架',
                                          `visibility` tinyint(4) DEFAULT '1' COMMENT '可见权限: 1-公开, 2-私密(仅自己可见)',

                                          `publish_time`  datetime              DEFAULT NULL COMMENT '首次发布时间，草稿为空',
                                          `like_count`     int          NOT NULL DEFAULT 0 COMMENT '点赞数',
                                          `comment_count`  int          NOT NULL DEFAULT 0 COMMENT '评论数',
                                          `click_count`    int          NOT NULL DEFAULT 0 COMMENT '点击数',
                                          `share_count`    int          NOT NULL DEFAULT 0 COMMENT '转发数',
                                          `coin_count`     int          NOT NULL DEFAULT 0 COMMENT '投币总数',
                                          `favorite_count` int          NOT NULL DEFAULT 0 COMMENT '收藏数',

                                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          PRIMARY KEY (`article_id`),
                                          KEY `idx_user_id` (`user_id`),
                                          KEY `idx_category_id` (`category_id`),
                                          KEY `idx_collection_id` (`collection_id`),
                                          CONSTRAINT `fk_articles_user`
                                              FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`)
                                                  ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏主表';


CREATE TABLE IF NOT EXISTS `article_thumb` (
                                               `id`         bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
                                               `article_id` bigint NOT NULL COMMENT '专栏ID',
                                               `user_id`    bigint NOT NULL COMMENT '点赞用户ID',
                                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
                                               PRIMARY KEY (`id`),
                                               UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),  -- 同一人只能赞一次
                                               KEY `idx_user_id` (`user_id`),
                                               CONSTRAINT `fk_article_like_article`
                                                   FOREIGN KEY (`article_id`) REFERENCES `articles` (`article_id`)
                                                       ON UPDATE CASCADE ON DELETE CASCADE,
                                               CONSTRAINT `fk_article_like_user`
                                                   FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`)  -- 按你 user 表主键改
                                                       ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏点赞表';

-- ========== 专栏投币（一人一篇一次，默认 1 币；余额在 user_wallet） ==========
CREATE TABLE IF NOT EXISTS `article_coin` (
                                              `id`          bigint   NOT NULL AUTO_INCREMENT,
                                              `article_id`  bigint   NOT NULL COMMENT '专栏ID',
                                              `user_id`     bigint   NOT NULL COMMENT '投币者 u_id',-- 可以关联到user_wallet
                                              `coin_num`    int      NOT NULL DEFAULT 1 COMMENT '本次投币数，本项目固定 1',
                                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              PRIMARY KEY (`id`),
                                              UNIQUE KEY `uk_article_user` (`article_id`, `user_id`), -- 防止用户重复给一篇文章投币
                                              KEY `idx_user_id` (`user_id`),
                                              CONSTRAINT `fk_acoin_article`
                                                  FOREIGN KEY (`article_id`) REFERENCES `articles` (`article_id`)  -- 和articles表关联
                                                      ON UPDATE CASCADE ON DELETE CASCADE,
                                              CONSTRAINT `fk_acoin_user`
                                                  FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`) -- 和user表关联
                                                      ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏投币关系表';

-- ========== 专栏收藏（读者收藏，区别于 articles.collection_id 作者文集） ==========
CREATE TABLE IF NOT EXISTS `article_favorite` (
                                                  `id`          bigint   NOT NULL AUTO_INCREMENT,
                                                  `article_id`  bigint   NOT NULL COMMENT '专栏ID',
                                                  `user_id`     bigint   NOT NULL COMMENT '收藏者 u_id',
                                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  PRIMARY KEY (`id`),
                                                  UNIQUE KEY `uk_article_user` (`article_id`, `user_id`),
                                                  KEY `idx_user_id` (`user_id`),
                                                  CONSTRAINT `fk_afav_article`
                                                      FOREIGN KEY (`article_id`) REFERENCES `articles` (`article_id`)
                                                          ON UPDATE CASCADE ON DELETE CASCADE,
                                                  CONSTRAINT `fk_afav_user`
                                                      FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`)
                                                          ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏收藏表';

-- ========== 专栏转发（可选记人；只要数字也可只改 share_count） ==========
CREATE TABLE IF NOT EXISTS `article_share` (
                                               `id`          bigint   NOT NULL AUTO_INCREMENT,
                                               `article_id`  bigint   NOT NULL COMMENT '专栏ID',
                                               `user_id`     bigint   NOT NULL COMMENT '转发者 u_id',
                                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               PRIMARY KEY (`id`),
                                               KEY `idx_article_id` (`article_id`),
                                               KEY `idx_user_id` (`user_id`),
                                               CONSTRAINT `fk_ashare_article`
                                                   FOREIGN KEY (`article_id`) REFERENCES `articles` (`article_id`)
                                                       ON UPDATE CASCADE ON DELETE CASCADE,
                                               CONSTRAINT `fk_ashare_user`
                                                   FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`)
                                                       ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏转发表';

CREATE TABLE IF NOT EXISTS `article_tag`(
                                            `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
                                            `name` varchar(64) NOT NULL COMMENT '标签名',
                                            `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `is_delete` tinyint default 0 not null comment '是否删除，0未删除，1已删除',
                                            PRIMARY KEY (`id`),
                                            UNIQUE KEY `uk_name` (`name`) -- 可选：标签名不能重复，加上唯一索引
);
CREATE TABLE IF NOT EXISTS `article_tag_relation` (
                                                      `id`          bigint   NOT NULL AUTO_INCREMENT COMMENT '主键',
                                                      `article_id`  bigint   NOT NULL COMMENT '专栏ID',
                                                      `tag_id`      bigint      NOT NULL COMMENT '标签ID',
                                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '挂载时间',
                                                      PRIMARY KEY (`id`),
                                                      UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
                                                      KEY `idx_tag_id` (`tag_id`),
                                                      CONSTRAINT `fk_atr_article`
                                                          FOREIGN KEY (`article_id`) REFERENCES `articles` (`article_id`)
                                                              ON UPDATE CASCADE ON DELETE CASCADE,
                                                      CONSTRAINT `fk_atr_tag`
                                                          FOREIGN KEY (`tag_id`) REFERENCES `article_tag` (`id`)
                                                              ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏标签关联表';

-- ========== 评论 ==========
CREATE TABLE IF NOT EXISTS `article_comment` (
                                                 `comment_id`         bigint        NOT NULL AUTO_INCREMENT,
                                                 `article_id`         bigint        NOT NULL,
                                                 `user_id`            bigint        NOT NULL COMMENT '评论者UID',
                                                 `content`            varchar(2000) NOT NULL,
                                                 `root_comment_id`    bigint        NOT NULL DEFAULT 0 COMMENT '一级=0；二级=根评id',
                                                 `to_comment_id`      bigint                 DEFAULT NULL COMMENT '回复的目标评论id',
                                                 `reply_to_user_id`   bigint                 DEFAULT NULL COMMENT '被回复用户UID',
                                                 `comment_like_count` int           NOT NULL DEFAULT 0,
                                                 `reply_count`        int           NOT NULL DEFAULT 0 COMMENT '根评下的回复数',
                                                 `create_time`        datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 `is_delete`          tinyint       NOT NULL DEFAULT 0,
                                                 PRIMARY KEY (`comment_id`),
                                                 KEY `idx_article_root` (`article_id`, `root_comment_id`),
                                                 KEY `idx_user_id` (`user_id`),
                                                 CONSTRAINT `fk_ac_article`
                                                     FOREIGN KEY (`article_id`) REFERENCES `articles` (`article_id`)
                                                         ON UPDATE CASCADE ON DELETE CASCADE,
                                                 CONSTRAINT `fk_ac_user`
                                                     FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`)
                                                         ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏评论';

-- ========== 评论点赞 ==========
CREATE TABLE IF NOT EXISTS `article_comment_thumb` (
                                                       `id`          bigint   NOT NULL AUTO_INCREMENT,
                                                       `comment_id`  bigint   NOT NULL,
                                                       `user_id`     bigint   NOT NULL,
                                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                       PRIMARY KEY (`id`),
                                                       UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
                                                       KEY `idx_user_id` (`user_id`),
                                                       CONSTRAINT `fk_act_comment`
                                                           FOREIGN KEY (`comment_id`) REFERENCES `article_comment` (`comment_id`)
                                                               ON UPDATE CASCADE ON DELETE CASCADE,
                                                       CONSTRAINT `fk_act_user`
                                                           FOREIGN KEY (`user_id`) REFERENCES `user` (`u_id`)
                                                               ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏评论点赞';

CREATE TABLE IF NOT EXISTS `article_category` (
                                                  `id`          int          NOT NULL AUTO_INCREMENT COMMENT '分类ID',
                                                  `name`        varchar(64)  NOT NULL COMMENT '分类名称，如游戏、动画、科技',
                                                  `sort`        int          NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前',
                                                  `status`      tinyint      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
                                                  `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                  PRIMARY KEY (`id`),
                                                  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏分类';

