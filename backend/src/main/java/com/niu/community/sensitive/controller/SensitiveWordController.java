package com.niu.community.sensitive.controller;

import com.niu.community.common.model.Result;
import com.niu.community.sensitive.service.SensitiveWordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensitive")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @GetMapping("/check")
    public Result<Boolean> check(@RequestParam String text) {
        return Result.success(sensitiveWordService.containsSensitiveWord(text));
    }
}
