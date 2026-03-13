package com.niu.community.notification.controller;

import com.niu.community.common.model.Result;
import com.niu.community.notification.entity.NotificationEntity;
import com.niu.community.notification.service.NotificationService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/list")
    public Result<List<NotificationEntity>> list(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("获取成功", notificationService.listByUserId(userId));
    }

    @GetMapping("/grouped")
    public Result<Map<String, List<NotificationEntity>>> grouped(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("获取成功", notificationService.groupedByType(userId));
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> stats(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("获取成功", notificationService.countStats(userId));
    }

    @PostMapping("/read-all")
    public Result<Void> readAll(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        notificationService.markAllRead(userId);
        return Result.success("已全部设为已读");
    }
}
