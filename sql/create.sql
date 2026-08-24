# 首先创建个人信息的表，用来给普通用户登录注册
USE platform;
CREATE TABLE IF NOT EXISTS `user`
(
    UID BIGINT AUTO_INCREMENT  COMMENT  '用户UID',
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
    UNIQUE KEY `uk_uid` (`UID`) -- 还有UID这个唯一索引
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

# 专栏
CREATE TABLE IF NOT EXISTS `article` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '专栏ID (即 cv 号)',
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

                           `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           PRIMARY KEY (`id`),
                           KEY `idx_user_id` (`user_id`),
                           KEY `idx_category_id` (`category_id`),
                           KEY `idx_collection_id` (`collection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专栏主表';


# CREATE TABLE IF NOT EXISTS `bili_picture` (
#     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '图片ID'
#
# );