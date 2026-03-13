package com.niu.community.auth.controller;

import com.niu.community.auth.dto.LoginRequest;
import com.niu.community.auth.dto.LoginResponse;
import com.niu.community.auth.dto.RegisterRequest;
import com.niu.community.auth.dto.SendCodeRequest;
import com.niu.community.auth.service.AuthService;
import com.niu.community.common.model.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody @Valid SendCodeRequest request) {
        authService.sendCode(request.getEmail());
        return Result.success("验证码已发送");
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody @Valid RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestAttribute(value = "userId", required = false) Long userId,
                               HttpServletRequest request) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        authService.logout(userId, request.getHeader("Authorization"));
        return Result.success("退出成功");
    }
}
