## emoticon pack
CREATE TABLE IF NOT EXISTS `emoticon_pack` (
                                              id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              name         VARCHAR(64)  NOT NULL COMMENT '包名',
                                              cover_url    VARCHAR(512) NULL COMMENT '封面相对路径',
                                              type         TINYINT      NOT NULL COMMENT '1官方小黄脸 2自定义',
                                              owner_uid    BIGINT       NULL COMMENT '自定义包所属用户，官方为NULL',
                                              status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
                                              sort         INT          NOT NULL DEFAULT 0,
                                              create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                              KEY idx_owner (owner_uid),
                                              KEY idx_type_status (type, status)

) COMMENT='表情包';

## emoticon item
CREATE TABLE IF NOT EXISTS `emoticon_item` (
                                              id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                                              pack_id      BIGINT       NOT NULL COMMENT 'emoticon_pack.id',
                                              code         VARCHAR(64)  NOT NULL COMMENT '短码，如 [微笑]',
                                              name         VARCHAR(64)  NULL COMMENT '展示名',
                                              url          VARCHAR(512) NOT NULL COMMENT '图片相对路径，长期有效',
                                              sort         INT          NOT NULL DEFAULT 0,
                                              status       TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0禁用',
                                              create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
#                                              unique key 在这里是唯一约束的意思，要保证pack_id和code是唯一的
                                              UNIQUE KEY uk_pack_code (pack_id, code),
                                              KEY idx_pack (pack_id)
)COMMENT='表情条目';