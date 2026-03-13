package com.niu.community.search.service;

import com.niu.community.common.model.PageResult;
import com.niu.community.es.service.PostEsService;
import com.niu.community.search.vo.SearchPostVO;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final PostEsService postEsService;

    public SearchService(PostEsService postEsService) {
        this.postEsService = postEsService;
    }

    public PageResult<SearchPostVO> search(String keyword, int page, int pageSize) {
        PageResult<com.niu.community.es.entity.PostDocument> esPage = postEsService.search(keyword, page, pageSize);
        return new PageResult<>(
            esPage.getList().stream().map(doc -> {
                SearchPostVO vo = new SearchPostVO();
                vo.setId(doc.getId());
                vo.setTitle(doc.getTitle());
                vo.setContent(doc.getContent());
                vo.setAuthorName(doc.getAuthorName());
                vo.setCreateTime(doc.getCreateTime());
                vo.setLikeCount(doc.getLikeCount());
                vo.setCommentCount(doc.getCommentCount());
                return vo;
            }).toList(),
            esPage.getTotal(),
            esPage.getPage(),
            esPage.getPageSize()
        );
    }
}
