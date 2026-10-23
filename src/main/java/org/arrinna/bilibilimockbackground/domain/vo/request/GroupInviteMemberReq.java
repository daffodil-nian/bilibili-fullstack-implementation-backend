package org.arrinna.bilibilimockbackground.domain.vo.request;

import lombok.Data;

import java.util.List;

/**
 * 邀请别人进群的req
 */
@Data
public class GroupInviteMemberReq {

    private List<Long> uids;

}
