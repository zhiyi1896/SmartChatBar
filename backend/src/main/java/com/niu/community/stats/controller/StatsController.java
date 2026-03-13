package com.niu.community.stats.controller;

import com.niu.community.common.model.Result;
import com.niu.community.permission.annotation.RequireRole;
import com.niu.community.stats.service.StatsService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/uv/record")
    public Result<Void> record(HttpServletRequest request) {
        statsService.recordUv(request.getRemoteAddr());
        return Result.success("记录成功");
    }

    @GetMapping("/uv/summary")
    @RequireRole({"ADMIN"})
    public Result<Map<String, Long>> summary() {
        return Result.success("查询成功", statsService.getUvSummary(7));
    }
}
