package org.arrinna.bilibilimockbackground.domain.vo.request;

import lombok.Data;

import java.util.List;

/**
 * 群聊创建申请
 */
@Data
public class CreateGroupReq {
    private String name;
    private List<Long> memberUids;
}
