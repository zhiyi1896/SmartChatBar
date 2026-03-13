package com.niu.community.search.controller;

import com.niu.community.common.model.PageResult;
import com.niu.community.common.model.Result;
import com.niu.community.search.service.SearchService;
import com.niu.community.search.vo.SearchPostVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping
    public Result<PageResult<SearchPostVO>> search(@RequestParam String keyword,
                                                   @RequestParam(defaultValue = "1") int page,
                                                   @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success("查询成功", searchService.search(keyword, page, pageSize));
    }
}
