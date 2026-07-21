package org.arrinna.bilibilimockbackground.domain.entity.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Builder;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@TableName(value = "user",autoResultMap = true)
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    @TableField(value = "u_id")
    private Long uId;//这个

    private Long id;

    private String nickname;   // 用户昵称

    private String username;   // 用户名

    private String password;   // 密码

    private Integer sex;       // 性别 0=保密 1=男 2=女

    private LocalDate birthDay; // 生日

    private String avatar;     // 头像URL

    private Long openId;       // 第三方登录openid

    @TableField(typeHandler = JacksonTypeHandler.class)
    private IpInfo ipInfo;     // IP信息（JSON类型）

    private Integer activeStatus; // 活跃状态 1=在线 2=离线

    private Integer status;    // 账号状态 1=正常 2=小黑屋 3=注销

    private Long itemId;       // 关联装扮/道具ID

    private Integer level;      // 等级 0-6

    private Integer needAddExp;        // 当前等级经验值
//    private Integer totalExp;   // 累计总经验

    private LocalDateTime createTime; // 创建时间

    private LocalDateTime updateTime; // 更新时间
}
