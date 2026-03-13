package com.niu.community.message.vo;

import lombok.Data;

@Data
public class ConversationVO {
    private Long conversationId;
    private Long userId;
    private String userName;
    private String userAvatar;
    private String lastMessage;
    private String lastMessageTime;
    private Integer unreadCount;
}
