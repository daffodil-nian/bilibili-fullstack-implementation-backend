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

