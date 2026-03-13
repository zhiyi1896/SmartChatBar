package com.niu.community.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    private Long userId;
    private Long senderId;
    private String type;
    private String content;
    private Long relatedId;
}
