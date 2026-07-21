package org.arrinna.bilibilimockbackground.domain.entity.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@TableName(value = "user_follow",autoResultMap = true)
public class UserFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;//关注表的id

    private Long userId;

    private Long followId;

    private Integer status;//是否取关

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
