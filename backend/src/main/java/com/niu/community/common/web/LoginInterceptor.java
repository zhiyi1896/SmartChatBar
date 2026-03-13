package com.niu.community.common.web;

import com.niu.community.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final AuthService authService;

    public LoginInterceptor(JwtUtils jwtUtils, AuthService authService) {
        this.jwtUtils = jwtUtils;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = authService.verifyLoginToken(token);
        if (userId == null) {
            userId = jwtUtils.getValidUserId(token);
        }
        if (userId != null) {
            request.setAttribute("userId", userId);
            com.niu.community.common.context.UserContext.setUserId(userId);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        com.niu.community.common.context.UserContext.clear();
    }
}
