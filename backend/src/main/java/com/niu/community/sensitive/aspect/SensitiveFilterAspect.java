package com.niu.community.sensitive.aspect;

import com.niu.community.common.exception.BusinessException;
import com.niu.community.sensitive.annotation.SensitiveFilter;
import com.niu.community.sensitive.service.SensitiveWordService;
import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SensitiveFilterAspect {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveFilterAspect(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @Around("@annotation(sensitiveFilter)")
    public Object around(ProceedingJoinPoint joinPoint, SensitiveFilter sensitiveFilter) throws Throwable {
        for (Object arg : joinPoint.getArgs()) {
            if (arg == null) {
                continue;
            }
            for (Field field : arg.getClass().getDeclaredFields()) {
                if (!field.getType().equals(String.class)) {
                    continue;
                }
                field.setAccessible(true);
                Object value = field.get(arg);
                if (value instanceof String text && sensitiveWordService.containsSensitiveWord(text)) {
                    throw new BusinessException("内容包含敏感词，请修改后再提交");
                }
            }
        }
        return joinPoint.proceed();
    }
}
