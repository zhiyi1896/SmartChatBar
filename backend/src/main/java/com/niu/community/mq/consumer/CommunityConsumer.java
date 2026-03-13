package com.niu.community.mq.consumer;

import com.niu.community.common.exception.BusinessException;
import com.niu.community.es.entity.PostDocument;
import com.niu.community.es.service.PostEsService;
import com.niu.community.mq.config.RabbitMqConfig;
import com.niu.community.mq.dto.NotificationMessage;
import com.niu.community.mq.dto.PostSyncMessage;
import com.niu.community.notification.service.NotificationService;
import com.niu.community.post.entity.PostEntity;
import com.niu.community.post.mapper.PostMapper;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import java.time.format.DateTimeFormatter;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CommunityConsumer {

    private final NotificationService notificationService;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final PostEsService postEsService;

    public CommunityConsumer(NotificationService notificationService, PostMapper postMapper,
                             UserMapper userMapper, PostEsService postEsService) {
        this.notificationService = notificationService;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
        this.postEsService = postEsService;
    }

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationMessage message) {
        notificationService.createNotification(message.getUserId(), message.getSenderId(), message.getType(), message.getContent(), message.getRelatedId());
    }

    @RabbitListener(queues = RabbitMqConfig.POST_SYNC_QUEUE)
    public void handlePostSync(PostSyncMessage message) {
        if ("DELETE".equalsIgnoreCase(message.getAction())) {
            postEsService.delete(message.getPostId());
            return;
        }
        PostEntity post = postMapper.selectById(message.getPostId());
        if (post == null) {
            throw new BusinessException("帖子不存在");
        }
        UserEntity user = userMapper.selectById(post.getUserId());
        PostDocument document = new PostDocument();
        document.setId(post.getId());
        document.setUserId(post.getUserId());
        document.setTitle(post.getTitle());
        document.setContent(post.getContent());
        document.setAuthorName(user == null ? "匿名" : user.getNickname());
        document.setLikeCount(post.getLikeCount());
        document.setCommentCount(post.getCommentCount());
        document.setViewCount(post.getViewCount());
        document.setCreateTime(post.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        postEsService.save(document);
    }
}
