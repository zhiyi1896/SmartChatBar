package com.niu.community.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentDTO {
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    private Long parentId;
    private Long replyUserId;

    @NotNull(message = "评论类型不能为空")
    private Integer type;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论长度不能超过1000")
    private String content;
}
