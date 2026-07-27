package org.arrinna.bilibilimockbackground.domain.dto.search;

import lombok.Data;
import org.arrinna.bilibilimockbackground.common.PageRequest;

import java.io.Serializable;

@Data
public class SearchRequest extends PageRequest implements Serializable {
    /**
     * 搜索关键词
     */
    private String keyword;
    /**
     * 类型
     */
    private String type;

}
