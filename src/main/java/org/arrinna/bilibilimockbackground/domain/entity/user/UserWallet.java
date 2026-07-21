package org.arrinna.bilibilimockbackground.domain.entity.user;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Builder
@TableName(value = "user_wallet",autoResultMap = true)
public class UserWallet implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long user_id;//存储的是用户的user_id，但是实际上不是UID，要分清楚！

    private Integer coin;//用户拥有的硬币数量

    private Integer bCoin;

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
