package com.niu.community.admin.controller;

import com.niu.community.common.model.Result;
import com.niu.community.permission.annotation.RequireRole;
import com.niu.community.post.service.PostService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequireRole({"ADMIN"})
public class AdminController {

    private final PostService postService;

    public AdminController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/post/{id}/top")
    public Result<Void> top(@PathVariable Long id) {
        postService.markTop(id, true);
        return Result.success("置顶成功");
    }

    @PostMapping("/post/{id}/untop")
    public Result<Void> untop(@PathVariable Long id) {
        postService.markTop(id, false);
        return Result.success("取消置顶成功");
    }

    @PostMapping("/post/{id}/wonderful")
    public Result<Void> wonderful(@PathVariable Long id) {
        postService.markWonderful(id, true);
        return Result.success("加精成功");
    }

    @PostMapping("/post/{id}/unwonderful")
    public Result<Void> unwonderful(@PathVariable Long id) {
        postService.markWonderful(id, false);
        return Result.success("取消加精成功");
    }
}
