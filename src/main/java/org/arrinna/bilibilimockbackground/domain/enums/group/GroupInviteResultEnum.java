package org.arrinna.bilibilimockbackground.domain.enums.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;


@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GroupInviteResultEnum {
    PENDING(1,"pending","待处理"),
    ACCEPTED(2,"accepted","已接受"),
    REJECTED(3,"rejected","已拒绝"),
    ;

    private Integer code;

    private String status;

    private String description;

//    private Map<>

}
