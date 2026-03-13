package com.niu.community.notification.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.niu.community.notification.entity.NotificationEntity;
import com.niu.community.notification.mapper.NotificationMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationService(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    public void createNotification(Long userId, Long senderId, String type, String content, Long relatedId) {
        NotificationEntity entity = new NotificationEntity();
        entity.setUserId(userId);
        entity.setSenderId(senderId);
        entity.setType(type);
        entity.setContent(content);
        entity.setRelatedId(relatedId);
        entity.setIsRead(0);
        entity.setCreateTime(LocalDateTime.now());
        notificationMapper.insert(entity);
    }

    public List<NotificationEntity> listByUserId(Long userId) {
        return notificationMapper.selectList(Wrappers.<NotificationEntity>lambdaQuery()
            .eq(NotificationEntity::getUserId, userId)
            .orderByDesc(NotificationEntity::getCreateTime));
    }

    public Map<String, Long> countStats(Long userId) {
        long total = notificationMapper.selectCount(Wrappers.<NotificationEntity>lambdaQuery().eq(NotificationEntity::getUserId, userId));
        long unread = notificationMapper.selectCount(Wrappers.<NotificationEntity>lambdaQuery().eq(NotificationEntity::getUserId, userId).eq(NotificationEntity::getIsRead, 0));
        Map<String, Long> result = new HashMap<>();
        result.put("total", total);
        result.put("unread", unread);
        result.put("likeUnread", countByType(userId, "LIKE"));
        result.put("commentUnread", countByType(userId, "COMMENT"));
        result.put("followUnread", countByType(userId, "FOLLOW"));
        return result;
    }

    public Map<String, List<NotificationEntity>> groupedByType(Long userId) {
        return listByUserId(userId).stream().collect(Collectors.groupingBy(NotificationEntity::getType));
    }

    @Transactional
    public void markAllRead(Long userId) {
        notificationMapper.update(
            null,
            Wrappers.<NotificationEntity>lambdaUpdate()
                .set(NotificationEntity::getIsRead, 1)
                .eq(NotificationEntity::getUserId, userId)
                .eq(NotificationEntity::getIsRead, 0)
        );
    }

    private long countByType(Long userId, String type) {
        return notificationMapper.selectCount(Wrappers.<NotificationEntity>lambdaQuery()
            .eq(NotificationEntity::getUserId, userId)
            .eq(NotificationEntity::getType, type)
            .eq(NotificationEntity::getIsRead, 0));
    }
}
