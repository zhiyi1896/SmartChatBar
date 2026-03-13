package com.niu.community.hot.controller;

import com.niu.community.common.model.PageResult;
import com.niu.community.common.model.Result;
import com.niu.community.post.vo.PostVO;
import com.niu.community.ranking.service.RankingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hot")
public class HotPostController {

    private final RankingService rankingService;

    public HotPostController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping("/weekly")
    public Result<PageResult<PostVO>> weekly(@RequestParam(defaultValue = "7") int days,
                                             @RequestParam(defaultValue = "10") int limit) {
        return Result.success("查询成功", rankingService.getHotPosts(1, limit));
    }
}
