package com.niu.community.permission.service;

import com.niu.community.common.exception.BusinessException;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final UserMapper userMapper;

    public PermissionService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public void checkRole(Long userId, String[] roles) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        UserEntity user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        for (String role : roles) {
            if (role.equalsIgnoreCase(user.getRole())) {
                return;
            }
        }
        throw new BusinessException("无权限访问");
    }
}
