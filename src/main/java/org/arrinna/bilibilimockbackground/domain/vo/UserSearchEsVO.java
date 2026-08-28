package org.arrinna.bilibilimockbackground.domain.vo;

import lombok.Data;
import org.arrinna.bilibilimockbackground.domain.enums.BiliLVEnum;

import java.io.Serializable;

/**
 * 自定义mapping
 */
@Data
public class UserSearchEsVO implements Serializable {
      private String nickname;
      private Integer fansCount;
      private Integer videoCount;
      private String avatar;
      private String signature;
      private Long userId;
      private BiliLVEnum biliLVEnum;
}
