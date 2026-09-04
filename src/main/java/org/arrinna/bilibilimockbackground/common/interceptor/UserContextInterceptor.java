package org.arrinna.bilibilimockbackground.common.interceptor;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.extra.servlet.ServletUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private static final List<String> WHITE_LIST = Arrays.asList(
            "/api/auth/login",
            "/api/auth/register",
            "/api/search/**",
            "/api/search/all"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = JakartaServletUtil.getClientIP(request);
        log.info("hihihi"+ip);
        String uri = request.getRequestURI();
        for (String pattern : WHITE_LIST) {
            if (MATCHER.match(pattern, uri)) {
                return true;
            }
        }

        //然后构造name...

        String uid = request.getHeader("uid");
        if (uid != null && !uid.isEmpty()) {
            request.setAttribute("uid", Long.parseLong(uid));
            return true;
        }
        response.setStatus(401);
        return false;
    }
}
