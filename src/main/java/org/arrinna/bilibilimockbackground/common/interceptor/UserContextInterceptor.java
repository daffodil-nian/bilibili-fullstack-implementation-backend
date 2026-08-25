package org.arrinna.bilibilimockbackground.common.interceptor;

import cn.hutool.core.text.AntPathMatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Component
public class UserContextInterceptor implements HandlerInterceptor {




    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler){
        //1.白名单
        AntPathMatcher matcher = new AntPathMatcher();
        List<String> whiteList = Arrays.asList(
                "/api/auth/login",
                "/api/auth/register"
        );
        String URI =request.getRequestURI();
        for (String path:whiteList){
            if(matcher.match(path,URI)){
                return true;
            }
        }
        String uid=request.getHeader("uid");
        if (uid != null && !uid.isEmpty()) {
            // 【最关键的一步】：把 uid 放入 request 作用域，属性名必须叫 "uid"
            request.setAttribute("uid", Long.parseLong(uid));
            return true;
        }

        // 没有拿到 uid，说明未登录，直接拒绝
        response.setStatus(401);
        return false;
    }
}
