package org.arrinna.bilibilimockbackground.common;

import lombok.Data;
import org.arrinna.bilibilimockbackground.common.constant.OrderConstant;

@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private long currentNum = 1;
    /**
     * 页面大小
     */
    private long pageSize = 20;

    private String sortedField;//排序字段

    /**
     * 排序1顺序1
     */
    private String sortOrder= OrderConstant.SORT_ORDER_ASC;//排序顺序
}
