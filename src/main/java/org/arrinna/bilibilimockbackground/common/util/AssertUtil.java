package org.arrinna.bilibilimockbackground.common.util;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import org.arrinna.bilibilimockbackground.common.exception.BusinessException;
import org.arrinna.bilibilimockbackground.common.exception.ErrorCodeEnum;

/**
 * 校验工具类
 */
public class AssertUtil {


    public static void isNotEmpty(Object obj, String errorMsg) {
        if (ObjectUtils.isEmpty(obj)) {
            throw new BusinessException(errorMsg);
        }
    }

    public static void isNotEmpty(Object obj, ErrorCodeEnum error) {
        if (ObjectUtils.isEmpty(obj)) {
            throw new BusinessException(error);
        }
    }

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
    public static void isFalse(boolean expression, ErrorCodeEnum error){
        if(expression){
            throw new BusinessException(error);
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

    public static void isTrue(boolean expression, ErrorCodeEnum error){
        if(!expression){
            throw new BusinessException(error);
        }
    }
}
