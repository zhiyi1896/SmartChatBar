package com.niu.community.post.controller;

import com.niu.community.common.model.PageResult;
import com.niu.community.common.model.Result;
import com.niu.community.permission.annotation.RequireRole;
import com.niu.community.post.dto.PostDTO;
import com.niu.community.post.service.PostService;
import com.niu.community.post.vo.PostVO;
import com.niu.community.sensitive.annotation.SensitiveFilter;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/publish")
    @SensitiveFilter
    public Result<PostVO> publishPost(@RequestBody @Valid PostDTO postDTO,
                                      @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("发布成功", postService.publishPost(postDTO, userId));
    }

    @GetMapping("/list")
    public Result<PageResult<PostVO>> getPostList(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int pageSize,
                                                  @RequestParam(required = false) Long userId,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        return Result.success("查询成功", postService.getPostList(page, pageSize, userId, keyword, currentUserId));
    }

    @GetMapping("/detail/{id}")
    public Result<PostVO> getPostDetail(@PathVariable Long id,
                                        @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        return Result.success("查询成功", postService.getPostDetail(id, currentUserId));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deletePost(@PathVariable Long id,
                                   @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        postService.deletePost(id, userId);
        return Result.success("删除成功");
    }

    @PostMapping("/wonderful/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> markWonderful(@PathVariable Long id) {
        return Result.success("预留接口，后续接入置顶加精逻辑");
    }
}
