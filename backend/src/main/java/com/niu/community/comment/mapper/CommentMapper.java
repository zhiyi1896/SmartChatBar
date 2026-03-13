package com.niu.community.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.niu.community.comment.entity.CommentEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {
    List<Map<String, Object>> selectCommentTree(@Param("targetId") Long targetId, @Param("type") Integer type);
}
