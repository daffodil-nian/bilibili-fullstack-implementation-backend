package org.arrinna.bilibilimockbackground.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

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
        String uri = request.getRequestURI();
        for (String pattern : WHITE_LIST) {
            if (MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        String uid = request.getHeader("uid");
        if (uid != null && !uid.isEmpty()) {
            request.setAttribute("uid", Long.parseLong(uid));
            return true;
        }
        response.setStatus(401);
        return false;
    }
}
