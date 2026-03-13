package com.niu.community.notification.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("notification")
public class NotificationEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long senderId;
    private String type;
    private String content;
    private Long relatedId;
    private Integer isRead;
    private LocalDateTime createTime;
}
