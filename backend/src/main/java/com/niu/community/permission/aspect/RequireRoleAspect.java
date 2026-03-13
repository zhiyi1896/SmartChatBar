package com.niu.community.permission.aspect;

import com.niu.community.common.context.UserContext;
import com.niu.community.permission.annotation.RequireRole;
import com.niu.community.permission.service.PermissionService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RequireRoleAspect {

    private final PermissionService permissionService;

    public RequireRoleAspect(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Around("@annotation(requireRole)")
    public Object around(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        permissionService.checkRole(UserContext.getUserId(), requireRole.value());
        return joinPoint.proceed();
    }
}
