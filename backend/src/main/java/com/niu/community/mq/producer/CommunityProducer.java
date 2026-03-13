package com.niu.community.mq.producer;

import com.niu.community.mq.config.RabbitMqConfig;
import com.niu.community.mq.dto.NotificationMessage;
import com.niu.community.mq.dto.PostSyncMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class CommunityProducer {

    private final RabbitTemplate rabbitTemplate;

    public CommunityProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(NotificationMessage message) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.NOTIFICATION_EXCHANGE, "notification", message);
    }

    public void syncPost(PostSyncMessage message) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.POST_SYNC_EXCHANGE, "post.sync", message);
    }
}
