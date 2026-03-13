package com.niu.community.comment.vo;

import java.util.List;
import lombok.Data;

@Data
public class CommentVO {
    private Long id;
    private Long userId;
    private Long parentId;
    private Long replyUserId;
    private String authorName;
    private String authorAvatar;
    private String replyUserName;
    private String content;
    private Integer likeCount;
    private String createTime;
    private List<CommentVO> children;
}
