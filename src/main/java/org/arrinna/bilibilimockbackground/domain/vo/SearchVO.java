package org.arrinna.bilibilimockbackground.domain.vo;

import lombok.Data;
import org.arrinna.bilibilimockbackground.common.PageRequest;
//import org.arrinna.bilibilimockbackground.domain.vo.response.UserInfoResp;

import java.io.Serializable;
import java.util.List;

@Data
public class SearchVO extends PageRequest implements Serializable {
    private List<UserVO> userList;
}
