package com.niu.community.follow.controller;

import com.niu.community.common.model.Result;
import com.niu.community.follow.dto.FollowRequest;
import com.niu.community.follow.service.FollowService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggle(@RequestBody @Valid FollowRequest request,
                                              @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        if (userId.equals(request.getTargetUserId())) {
            return Result.error(400, "不能关注自己");
        }
        return Result.success("操作成功", followService.toggleFollow(userId, request.getTargetUserId()));
    }
}
