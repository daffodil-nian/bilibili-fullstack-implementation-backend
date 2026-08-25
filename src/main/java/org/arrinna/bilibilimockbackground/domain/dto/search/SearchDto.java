package org.arrinna.bilibilimockbackground.domain.dto.search;

import lombok.Data;
import org.arrinna.bilibilimockbackground.common.PageRequest;

import java.io.Serializable;

@Data
public class SearchDto extends PageRequest implements Serializable {
    /**
     * 搜索关键词
     */
    private String keyword;
    /**
     * 类型,比如用户类型，比如lv类型
     */
    private String searchType;


    private Integer sortCode;

}
