package com.niu.community.comment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("comment")
public class CommentEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long targetId;
    private Long parentId;
    private Long userId;
    private Long replyUserId;
    private Integer type;
    private String content;
    private Integer likeCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
