package com.niu.community.post.vo;

import lombok.Data;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String authorName;
    private String authorAvatar;
    private String createTime;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean liked;
    private Boolean author;
}
