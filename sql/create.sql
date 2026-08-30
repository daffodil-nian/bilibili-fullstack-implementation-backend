# 首先创建个人信息的表，用来给普通用户登录注册
USE platform;
CREATE TABLE IF NOT EXISTS `user`
(
    u_id BIGINT AUTO_INCREMENT  COMMENT  '用户UID',
    id BIGINT PRIMARY KEY COMMENT '用户ID', -- 这是用户的ID，主键自增
    nickname VARCHAR(20) NOT NULL COMMENT '用户昵称', -- 用户昵称,如果不写就用上指定的算法随机生成，如用户1232432432
    username VARCHAR(255) NOT NULL COMMENT '用户名', -- 用户名，也就是用户的登录的
    password VARCHAR(255) NOT NULL COMMENT '密码', -- 用户密码存储，密文存储
    sex TINYINT NOT NULL DEFAULT 0 COMMENT '性别 0=保密 1=男 2=女', -- 性别，0=保密 1=男 2=女
    birth_day DATE NOT NULL COMMENT '生日', -- 生日
    avatar VARCHAR(255) NOT NULL,
    -- 然后不建议user_level单独创建一张表，因为不好维护
    open_id BIGINT NOT NULL DEFAULT 0 COMMENT '第三方登录openid', -- 第三方登录openid
    ip_info JSON NOT NULL COMMENT 'IP信息（JSON类型）', -- IP信息（JSON类型）
    active_status TINYINT NOT NULL DEFAULT 1 COMMENT '活跃状态 1=在线 2=离线', -- 活跃状态 1=在线 2=离线
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态 1=正常 2=被关进小黑屋 3=用户注销状态', -- 账号状态 1=正常 2=小黑屋 3=注销
    level         int      default 0                 null comment 'B站等级',
    needAddExp    int      default 0                 null comment '需要新增的经验',
    item_id BIGINT NOT NULL DEFAULT 0 COMMENT '关联装扮/道具ID，这个可以扩展为外键', -- 关联装扮/道具ID
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', -- 创建时间
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间', -- 更新时间
    UNIQUE KEY `uk_username` (`username`), -- 用户名唯一索引
    UNIQUE KEY `uk_uid` (`u_id`) -- 还有UID这个唯一索引
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户基础信息表';

CREATE TABLE IF NOT EXISTS `wx_msg`
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    open_id VARCHAR(64) NOT NULL COMMENT '微信公众号openid',
    msg TEXT NOT NULL COMMENT '消息内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', -- 创建时间
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' -- 更新时间
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_wallet`
(
    user_id BIGINT PRIMARY KEY COMMENT '用户ID,与user表id一一对应', #这个和用户对应的id关联
    coin INT NOT NULL DEFAULT 0 COMMENT '硬币', -- 硬币
    b_coin INT NOT NULL DEFAULT 0 COMMENT 'b币',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间', -- 创建时间
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' -- 更新时间
) ENGINE =InnoDB DEFAULT CHARSET =utf8mb4;

# 然后就是用户关注1列表
CREATE TABLE IF NOT EXISTS `user_follow`
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    user_id BIGINT NOT NULL COMMENT '关注者ID',
    follow_id BIGINT NOT NULL COMMENT '被关注者ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1=关注中 2=已取关',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_follow` (`user_id`,`follow_id`),
    INDEX `idx_user_follow` (`follow_id`)
);
# CREATE TABLE IF NOT EXISTS `user_dynamic`
# (
#     id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '动态的ID',
#     user_id BIGINT NOT NULL COMMENT '发布者的ID',
#     content TEXT NOT NULL COMMENT '用户发布的内容',
#     dynamic_type TINYINT NOT NULL DEFAULT 1 COMMENT '动态类型 1=文字 2=图片',
#
# );

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

# 测试数据
INSERT INTO `user` (`username`, `password`, `nickname`, `create_time`) VALUES
                                                                           ('testuser01', 'Abc123@#01', '测试用户01', NOW()),
                                                                           ('testuser02', 'Def456$%02', '测试用户02', NOW()),
                                                                           ('testuser03', 'Ghi789^&03', '测试用户03', NOW()),
                                                                           ('testuser04', 'Jkl012*!04', '测试用户04', NOW()),
                                                                           ('testuser05', 'Mno345@#05', '测试用户05', NOW()),
                                                                           ('testuser06', 'Pqr678$%06', '测试用户06', NOW()),
                                                                           ('testuser07', 'Stu901^&07', '测试用户07', NOW()),
                                                                           ('testuser08', 'Vwx234*!08', '测试用户08', NOW()),
                                                                           ('testuser09', 'Yza567@#09', '测试用户09', NOW()),
                                                                           ('testuser10', 'Bcd890$%10', '测试用户10', NOW()),
                                                                           ('testuser11', 'Efg123^&11', '测试用户11', NOW()),
                                                                           ('testuser12', 'Hij456*!12', '测试用户12', NOW()),
                                                                           ('testuser13', 'Klm789@#13', '测试用户13', NOW()),
                                                                           ('testuser14', 'Nop012$%14', '测试用户14', NOW()),
                                                                           ('testuser15', 'Qrs345^&15', '测试用户15', NOW()),
                                                                           ('testuser16', 'Tuv678*!16', '测试用户16', NOW()),
                                                                           ('testuser17', 'Wxy901@#17', '测试用户17', NOW()),
                                                                           ('testuser18', 'Zab234$%18', '测试用户18', NOW()),
                                                                           ('testuser19', 'Cde567^&19', '测试用户19', NOW()),
                                                                           ('testuser20', 'Fgh890*!20', '测试用户20', NOW());



# ========= 已有库增量（库已建过 articles 时执行） ================
# ALTER TABLE articles
#     ADD COLUMN coin_count int NOT NULL DEFAULT 0 COMMENT '投币总数' AFTER share_count,
#     ADD COLUMN favorite_count int NOT NULL DEFAULT 0 COMMENT '收藏数' AFTER coin_count;
# 然后单独执行上面 CREATE TABLE article_coin / article_favorite / article_share
