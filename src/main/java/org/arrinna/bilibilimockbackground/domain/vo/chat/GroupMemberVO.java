package org.arrinna.bilibilimockbackground.domain.vo.chat;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//后端
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberVO {

    private Long uid;

    private String name;

    private String avatar;

    private Integer role;

    private Integer status;

}
