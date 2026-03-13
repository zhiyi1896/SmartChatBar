package com.niu.community.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.niu.community.message.entity.MessageEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MessageMapper extends BaseMapper<MessageEntity> {
    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} ORDER BY create_time DESC LIMIT 30")
    List<MessageEntity> selectRecentMessages(@Param("conversationId") Long conversationId);

    @Select("SELECT * FROM message WHERE conversation_id = #{conversationId} AND create_time < #{beforeTime} ORDER BY create_time DESC LIMIT 20")
    List<MessageEntity> selectEarlierMessages(@Param("conversationId") Long conversationId, @Param("beforeTime") LocalDateTime beforeTime);

    @Update("UPDATE message SET is_read = 1 WHERE conversation_id = #{conversationId} AND to_user_id = #{userId}")
    int markAsRead(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
