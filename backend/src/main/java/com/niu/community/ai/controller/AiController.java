package com.niu.community.ai.controller;

import com.niu.community.ai.dto.AiAskRequest;
import com.niu.community.ai.service.AiService;
import com.niu.community.common.model.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/ask")
    public Result<String> ask(@RequestBody AiAskRequest request) {
        return Result.success("获取成功", aiService.ask(request.getQuery(), Long.parseLong(request.getUserId())));
    }

    @PostMapping("/ask/user")
    public Result<String> userAsk(@RequestParam String query,
                                  @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("获取成功", aiService.ask(query, userId));
    }

    @DeleteMapping("/session")
    public Result<Void> clearSession(@RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        aiService.clearSession(userId);
        return Result.success("会话已清空");
    }
}
