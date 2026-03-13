package com.niu.community.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostDTO {
    private Long id;
    private Long userId;

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 100, message = "标题长度在1-100之间")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(min = 1, max = 10000, message = "内容长度在1-10000之间")
    private String content;
}
