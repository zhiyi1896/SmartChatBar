package com.niu.community.auth.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.niu.community.ai.service.AiService;
import com.niu.community.auth.dto.LoginRequest;
import com.niu.community.auth.dto.LoginResponse;
import com.niu.community.auth.dto.RegisterRequest;
import com.niu.community.common.exception.BusinessException;
import com.niu.community.common.web.JwtUtils;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final long LOGIN_HOURS = 72L;

    private final UserMapper userMapper;
    private final JavaMailSender javaMailSender;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AiService aiService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public AuthService(UserMapper userMapper, JavaMailSender javaMailSender, StringRedisTemplate redisTemplate,
                       PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AiService aiService) {
        this.userMapper = userMapper;
        this.javaMailSender = javaMailSender;
        this.redisTemplate = redisTemplate;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.aiService = aiService;
    }

    public void sendCode(String email) {
        long count = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getEmail, email));
        if (count > 0) {
            throw new BusinessException("该邮箱已注册");
        }
        String limitKey = "SEND_LIMIT:" + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(limitKey))) {
            throw new BusinessException("发送太频繁，请稍后再试");
        }
        String code = String.format("%06d", new Random().nextInt(999999));
        redisTemplate.opsForValue().set("REG_CODE:" + email, code, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(limitKey, "1", 60, TimeUnit.SECONDS);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("牛客社区验证码");
        message.setText("您的验证码是：" + code + "，5分钟内有效。");
        javaMailSender.send(message);
    }

    public LoginResponse register(RegisterRequest request) {
        String cacheCode = redisTemplate.opsForValue().get("REG_CODE:" + request.getEmail());
        if (cacheCode == null || !cacheCode.equals(request.getCode())) {
            throw new BusinessException("验证码错误或已过期");
        }
        long count = userMapper.selectCount(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getEmail, request.getEmail()));
        if (count > 0) {
            throw new BusinessException("该邮箱已注册");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setRole("USER");
        user.setStatus(1);
        user.setReceivedLikeCount(0);
        userMapper.insert(user);
        redisTemplate.delete("REG_CODE:" + request.getEmail());
        return buildLoginResponse(user, false);
    }

    public LoginResponse login(LoginRequest request) {
        UserEntity user = userMapper.selectOne(Wrappers.<UserEntity>lambdaQuery().eq(UserEntity::getEmail, request.getEmail()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("邮箱或密码错误");
        }
        return buildLoginResponse(user, true);
    }

    public void logout(Long userId, String token) {
        String pureToken = normalizeToken(token);
        if (pureToken != null) {
            redisTemplate.delete("LOGIN_TOKEN:" + pureToken);
        }
        aiService.clearSession(userId);
    }

    public Long verifyLoginToken(String token) {
        String pureToken = normalizeToken(token);
        if (pureToken == null) {
            return null;
        }
        Long userId = jwtUtils.getValidUserId(pureToken);
        if (userId == null) {
            return null;
        }
        String currentToken = redisTemplate.opsForValue().get("LOGIN_USER:" + userId);
        if (currentToken == null || !currentToken.equals(pureToken)) {
            return null;
        }
        String cached = redisTemplate.opsForValue().get("LOGIN_TOKEN:" + pureToken);
        return cached == null ? null : userId;
    }

    public void invalidateUserSessions(Long userId) {
        String currentToken = redisTemplate.opsForValue().get("LOGIN_USER:" + userId);
        if (currentToken != null) {
            redisTemplate.delete("LOGIN_TOKEN:" + currentToken);
            redisTemplate.delete("LOGIN_USER:" + userId);
        }
    }

    private LoginResponse buildLoginResponse(UserEntity user, boolean singleLogin) {
        if (singleLogin) {
            invalidateUserSessions(user.getId());
        }
        String token = jwtUtils.generateToken(user.getId());
        redisTemplate.opsForValue().set("LOGIN_TOKEN:" + token, String.valueOf(user.getId()), LOGIN_HOURS, TimeUnit.HOURS);
        redisTemplate.opsForValue().set("LOGIN_USER:" + user.getId(), token, LOGIN_HOURS, TimeUnit.HOURS);
        return new LoginResponse(token, user.getId(), user.getNickname(), user.getAvatar());
    }

    private String normalizeToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return token.startsWith("Bearer ") ? token.substring(7) : token;
    }
}
