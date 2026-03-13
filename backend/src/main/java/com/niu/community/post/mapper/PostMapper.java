package com.niu.community.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.niu.community.post.entity.PostEntity;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PostMapper extends BaseMapper<PostEntity> {
    List<Map<String, Object>> selectPostListWithUser(@Param("offset") int offset,
                                                     @Param("pageSize") int pageSize,
                                                     @Param("userId") Long userId,
                                                     @Param("keyword") String keyword);

    Map<String, Object> selectPostDetailWithUser(@Param("postId") Long postId);

    @Update("UPDATE post SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    @Update("UPDATE post SET comment_count = comment_count + 1 WHERE id = #{id} AND status = 1")
    int incrementCommentCount(@Param("id") Long id);
}
