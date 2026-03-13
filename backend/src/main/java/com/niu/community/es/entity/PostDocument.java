package com.niu.community.es.entity;

import lombok.Data;

@Data
public class PostDocument {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String authorName;
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private String createTime;
}
