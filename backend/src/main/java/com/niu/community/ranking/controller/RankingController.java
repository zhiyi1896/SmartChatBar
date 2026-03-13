package com.niu.community.ranking.controller;

import com.niu.community.common.model.PageResult;
import com.niu.community.common.model.Result;
import com.niu.community.post.vo.PostVO;
import com.niu.community.ranking.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/hot")
    public Result<PageResult<PostVO>> hot(@RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success("查询成功", rankingService.getHotPosts(page, pageSize));
    }
}
