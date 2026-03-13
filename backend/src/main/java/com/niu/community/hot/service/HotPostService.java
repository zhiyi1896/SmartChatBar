package com.niu.community.hot.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.niu.community.common.model.PageResult;
import com.niu.community.post.entity.PostEntity;
import com.niu.community.post.mapper.PostMapper;
import com.niu.community.post.vo.PostVO;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class HotPostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;

    public HotPostService(PostMapper postMapper, UserMapper userMapper) {
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }

    public PageResult<PostVO> getWeeklyHotPosts(int limit) {
        List<PostEntity> posts = postMapper.selectList(Wrappers.<PostEntity>lambdaQuery()
            .eq(PostEntity::getStatus, 1)
            .orderByDesc(PostEntity::getLikeCount)
            .orderByDesc(PostEntity::getCommentCount)
            .orderByDesc(PostEntity::getViewCount)
            .last("LIMIT " + limit));
        Map<Long, UserEntity> userMap = userMapper.selectBatchIds(posts.stream().map(PostEntity::getUserId).toList())
            .stream().collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        List<PostVO> list = posts.stream().map(post -> {
            UserEntity user = userMap.get(post.getUserId());
            PostVO vo = new PostVO();
            vo.setId(post.getId());
            vo.setUserId(post.getUserId());
            vo.setTitle(post.getTitle());
            vo.setContent(post.getContent());
            vo.setAuthorName(user == null ? "匿名" : user.getNickname());
            vo.setAuthorAvatar(user == null ? null : user.getAvatar());
            vo.setCreateTime(post.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            vo.setViewCount(post.getViewCount());
            vo.setLikeCount(post.getLikeCount());
            vo.setCommentCount(post.getCommentCount());
            vo.setLiked(false);
            vo.setAuthor(false);
            return vo;
        }).toList();
        return new PageResult<>(list, list.size(), 1, limit);
    }
}
