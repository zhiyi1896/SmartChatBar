package com.niu.community.message.dto;

import lombok.Data;

@Data
public class MessageDTO {
    private Long id;
    private Long conversationId;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private Integer isRead;
    private String createTime;
    private String fromUserNickname;
    private String fromUserAvatar;
}
