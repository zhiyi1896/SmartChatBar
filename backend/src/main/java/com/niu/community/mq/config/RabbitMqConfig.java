package com.niu.community.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;

@Configuration
public class RabbitMqConfig {

    public static final String NOTIFICATION_EXCHANGE = "community.notification.exchange";
    public static final String NOTIFICATION_QUEUE = "community.notification.queue";
    public static final String POST_SYNC_EXCHANGE = "community.post.exchange";
    public static final String POST_SYNC_QUEUE = "community.post.queue";
    public static final String DEAD_LETTER_EXCHANGE = "community.dead-letter.exchange";
    public static final String NOTIFICATION_DEAD_QUEUE = "community.notification.dead.queue";
    public static final String POST_SYNC_DEAD_QUEUE = "community.post.dead.queue";

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
            .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", NOTIFICATION_QUEUE)
            .build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue()).to(notificationExchange()).with("notification");
    }

    @Bean
    public DirectExchange postSyncExchange() {
        return new DirectExchange(POST_SYNC_EXCHANGE, true, false);
    }

    @Bean
    public Queue postSyncQueue() {
        return QueueBuilder.durable(POST_SYNC_QUEUE)
            .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", POST_SYNC_QUEUE)
            .build();
    }

    @Bean
    public Binding postSyncBinding() {
        return BindingBuilder.bind(postSyncQueue()).to(postSyncExchange()).with("post.sync");
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    public Queue notificationDeadQueue() {
        return QueueBuilder.durable(NOTIFICATION_DEAD_QUEUE).build();
    }

    @Bean
    public Queue postSyncDeadQueue() {
        return QueueBuilder.durable(POST_SYNC_DEAD_QUEUE).build();
    }

    @Bean
    public Binding notificationDeadBinding() {
        return BindingBuilder.bind(notificationDeadQueue()).to(deadLetterExchange()).with(NOTIFICATION_QUEUE);
    }

    @Bean
    public Binding postSyncDeadBinding() {
        return BindingBuilder.bind(postSyncDeadQueue()).to(deadLetterExchange()).with(POST_SYNC_QUEUE);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        rabbitTemplate.setBeforePublishPostProcessors(message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
                                                                               MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
            .maxAttempts(3)
            .backOffOptions(1000, 2.0, 5000)
            .build());
        return factory;
    }
}
