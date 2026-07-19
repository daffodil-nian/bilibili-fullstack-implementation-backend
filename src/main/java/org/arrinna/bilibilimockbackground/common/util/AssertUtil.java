package org.arrinna.bilibilimockbackground.common.util;

import org.arrinna.bilibilimockbackground.common.exception.BusinessException;

/**
 * 校验工具类
 */
public class AssertUtil {

    /**
     * 表达式为真为假，如表达式为空抛异常
     * @param expression
     * @param msg
     */
    public static void isFalse(boolean expression,String msg){
        if(expression){
            throw new BusinessException(msg);
        }
    }

    /**
     * 表达式为假为真，例如不满足某个条件就抛出异常
     * @param expression
     * @param msg
     */
    public static void isTrue(boolean expression,String msg){
        if(!expression){
            throw new BusinessException(msg);
        }
    }

}
