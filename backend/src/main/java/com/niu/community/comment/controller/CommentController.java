package com.niu.community.comment.controller;

import com.niu.community.comment.dto.CommentDTO;
import com.niu.community.comment.service.CommentService;
import com.niu.community.comment.vo.CommentVO;
import com.niu.community.common.model.Result;
import com.niu.community.sensitive.annotation.SensitiveFilter;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comment")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/publish")
    @SensitiveFilter
    public Result<Void> publishComment(@RequestBody @Valid CommentDTO commentDTO,
                                       @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        commentService.publishComment(commentDTO, userId);
        return Result.success("评论成功");
    }

    @GetMapping("/list")
    public Result<List<CommentVO>> getComments(@RequestParam Long targetId, @RequestParam Integer type) {
        return Result.success("查询成功", commentService.getComments(targetId, type));
    }
}
