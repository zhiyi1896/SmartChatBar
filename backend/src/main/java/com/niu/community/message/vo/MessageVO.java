package com.niu.community.message.vo;

import lombok.Data;

@Data
public class MessageVO {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private String time;
    private Boolean self;
    private Boolean read;
}
