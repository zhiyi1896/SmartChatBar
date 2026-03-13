package com.niu.community.profile.controller;

import com.niu.community.common.model.Result;
import com.niu.community.post.vo.PostVO;
import com.niu.community.profile.dto.UpdateEmailRequest;
import com.niu.community.profile.dto.UpdatePasswordRequest;
import com.niu.community.profile.dto.UpdateProfileRequest;
import com.niu.community.profile.service.ProfileService;
import com.niu.community.profile.vo.UserProfileVO;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public Result<UserProfileVO> getProfile(@PathVariable Long userId,
                                            @RequestAttribute(value = "userId", required = false) Long currentUserId) {
        return Result.success("查询成功", profileService.getProfile(userId, currentUserId));
    }

    @GetMapping("/{userId}/posts")
    public Result<List<PostVO>> posts(@PathVariable Long userId) {
        return Result.success("查询成功", profileService.getUserPosts(userId));
    }

    @PutMapping("/me")
    public Result<UserProfileVO> updateProfile(@RequestAttribute(value = "userId", required = false) Long userId,
                                               @RequestBody @Valid UpdateProfileRequest request) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("修改成功", profileService.updateProfile(userId, request));
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestAttribute(value = "userId", required = false) Long userId,
                                       @RequestBody @Valid UpdatePasswordRequest request) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        profileService.updatePassword(userId, request);
        return Result.success("修改成功");
    }

    @PostMapping("/email/code")
    public Result<Void> sendEmailCode(@RequestAttribute(value = "userId", required = false) Long userId,
                                      @RequestBody Map<String, String> body) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        profileService.sendEmailCode(userId, body.get("email"));
        return Result.success("验证码已发送");
    }

    @PutMapping("/email")
    public Result<Void> updateEmail(@RequestAttribute(value = "userId", required = false) Long userId,
                                    @RequestBody @Valid UpdateEmailRequest request) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        profileService.updateEmail(userId, request);
        return Result.success("修改成功");
    }
}
