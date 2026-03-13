package com.niu.community.search.dto;

import lombok.Data;

@Data
public class SearchRequest {
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 10;
}
