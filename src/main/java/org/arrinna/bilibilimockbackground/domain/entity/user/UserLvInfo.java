package org.arrinna.bilibilimockbackground.domain.entity.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserLvInfo {
    private Integer level;      // 等级 0-6
    private Integer needAddExp;// 当前等级经验值
    private Integer totalExp;   // 累计总经验
    private String  levelName;
}
