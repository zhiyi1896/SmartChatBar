package com.niu.community.search.vo;

import lombok.Data;

@Data
public class SearchPostVO {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private String createTime;
    private Integer likeCount;
    private Integer commentCount;
}
