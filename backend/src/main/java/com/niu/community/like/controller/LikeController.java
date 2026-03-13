package com.niu.community.like.controller;

import com.niu.community.common.model.Result;
import com.niu.community.like.dto.LikeRequest;
import com.niu.community.like.service.LikeService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/like")
public class LikeController {

    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggle(@RequestBody @Valid LikeRequest request,
                                              @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("操作成功", likeService.toggleLike(userId, request.getTargetType(), request.getTargetId(), request.getTargetOwnerId()));
    }
}
