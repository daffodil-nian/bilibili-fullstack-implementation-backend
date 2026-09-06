-- 房间（单聊/群聊的抽象）
CREATE TABLE IF NOT EXISTS chat_room (
                                         id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '房间ID',
                                         type         TINYINT NOT NULL COMMENT '1=群聊 2=单聊',
                                         status       TINYINT NOT NULL DEFAULT 0 COMMENT '0=正常 1=禁用/关闭',
                                         active_time  DATETIME NULL COMMENT '最后聊天活跃时间(列表排序用)',
                                         last_msg_id  BIGINT NULL COMMENT '最后一条消息ID',
                                         create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '行修改时间',
                                         KEY idx_type_active (type, active_time),
                                         KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IM房间';


-- 单聊房间扩展：两个 uid 对应一个 room
CREATE TABLE IF NOT EXISTS chat_room_friend (
                                                id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                room_id      BIGINT NOT NULL COMMENT 'chat_room.id',
                                                uid1         BIGINT NOT NULL COMMENT '较小的 user.u_id',
                                                uid2         BIGINT NOT NULL COMMENT '较大的 user.u_id',
                                                status       TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1禁用',
                                                create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                UNIQUE KEY uk_room_id (room_id),
                                                UNIQUE KEY uk_uid_pair (uid1, uid2),
                                                KEY idx_uid1 (uid1),
                                                KEY idx_uid2 (uid2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单聊房间扩展';


-- 群聊房间扩展
# CREATE TABLE room_group (
#                             id            BIGINT PRIMARY KEY AUTO_INCREMENT,
#                             room_id       BIGINT NOT NULL,
#                             name          VARCHAR(64) NOT NULL,
#                             avatar        VARCHAR(512) NULL,
#                             ext_json      VARCHAR(1024) NULL,
#                             delete_status INT NOT NULL DEFAULT 0 COMMENT '0正常 1删除',
#                             create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
#                             update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
#                             UNIQUE KEY uk_room_id (room_id)
# ) COMMENT='群聊房间';

-- 群成员（注意：group_id 存的是 room_group.id，和 MallChat 一致）
CREATE TABLE IF NOT EXISTS chat_room_group (
                                               id            BIGINT PRIMARY KEY AUTO_INCREMENT,
                                               room_id       BIGINT NOT NULL COMMENT 'chat_room.id',
                                               name          VARCHAR(64) NOT NULL COMMENT '群名称',
                                               avatar        VARCHAR(512) NULL COMMENT '群头像',
                                               owner_uid     BIGINT NOT NULL COMMENT '群主 user.u_id',
                                               delete_status TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1解散/删除',
                                               create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                               UNIQUE KEY uk_room_id (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群聊房间扩展';

-- ----------------------------
-- 群成员
-- ----------------------------
CREATE TABLE IF NOT EXISTS chat_group_member (
                                                 id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                                                 group_id     BIGINT NOT NULL COMMENT 'chat_room_group.id',
                                                 uid          BIGINT NOT NULL COMMENT 'user.u_id',
                                                 role         TINYINT NOT NULL COMMENT '1群主 2管理 3成员',
                                                 create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                 update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                 UNIQUE KEY uk_group_uid (group_id, uid),
                                                 KEY idx_uid (uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='群成员';


-- 我的会话列表（每人每个房间一条）

CREATE TABLE IF NOT EXISTS chat_contact (
                                            id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            uid          BIGINT NOT NULL COMMENT '当前用户 user.u_id',
                                            room_id      BIGINT NOT NULL COMMENT 'chat_room.id',
                                            read_time    DATETIME NULL COMMENT '我读到的时间(算未读)',
                                            active_time  DATETIME NULL COMMENT '该会话对我的活跃时间(列表排序)',
                                            last_msg_id  BIGINT NULL COMMENT '最后一条消息ID',
                                            create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                            UNIQUE KEY uk_uid_room (uid, room_id),
                                            KEY idx_uid_active (uid, active_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话列表项';
# CREATE TABLE contact (
#                          id           BIGINT PRIMARY KEY AUTO_INCREMENT,
#                          uid          BIGINT NOT NULL,
#                          room_id      BIGINT NOT NULL,
#                          read_time    DATETIME NULL COMMENT '我读到的时间',
#                          active_time  DATETIME NULL COMMENT '该会话对我的活跃时间(列表排序)',
#                          last_msg_id  BIGINT NULL,
#                          create_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
#                          update_time  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
#                          UNIQUE KEY uk_uid_room (uid, room_id),
#                          KEY idx_uid_active (uid, active_time)
# ) COMMENT='会话列表项';


-- 消息

CREATE TABLE IF NOT EXISTS chat_message (
                                            id            BIGINT PRIMARY KEY AUTO_INCREMENT,
                                            room_id       BIGINT NOT NULL COMMENT 'chat_room.id',
                                            from_uid      BIGINT NOT NULL COMMENT '发送者 user.u_id',
                                            content       TEXT NULL COMMENT '文本/表情code/图片URL',
                                            type          TINYINT NOT NULL COMMENT '1文本 2表情 3图片',
                                            status        TINYINT NOT NULL DEFAULT 0 COMMENT '0正常 1已撤回',
                                            at_uids       VARCHAR(512) NULL COMMENT '群聊@，JSON如[1,2]，单聊为空',
                                            reply_msg_id  BIGINT NULL COMMENT '回复消息ID，可空',
                                            client_ip     VARCHAR(64) NULL COMMENT '发送方IP，仅服务端审计',
                                            ip_region     VARCHAR(64) NULL COMMENT 'IP属地展示，如省份',
                                            create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '聊天时间',
                                            update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                            KEY idx_room_id (room_id, id),
                                            KEY idx_from_uid (from_uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息';
# CREATE TABLE message (
#                          id            BIGINT PRIMARY KEY AUTO_INCREMENT,
#                          room_id       BIGINT NOT NULL,
#                          from_uid      BIGINT NOT NULL,
#                          content       TEXT NULL,
#                          reply_msg_id  BIGINT NULL COMMENT '回复哪条,可空',
#                          status        INT NOT NULL DEFAULT 0 COMMENT '0正常 1删除/撤回',
#                          gap_count     INT NULL COMMENT '与回复消息间隔,可空',
#                          type          INT NOT NULL COMMENT '1文本 2撤回 3表情 4图片...(按你枚举)',
#                          extra         JSON NULL COMMENT '扩展:图片url、@列表等',
#                          create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
#                          update_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
#                          KEY idx_room_id (room_id, id)
# ) COMMENT='消息';