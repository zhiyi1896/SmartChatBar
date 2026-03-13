package com.niu.community.message.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.niu.community.message.entity.ConversationEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ConversationMapper extends BaseMapper<ConversationEntity> {
    List<Map<String, Object>> selectConversations(@Param("userId") Long userId,
                                                  @Param("offset") int offset,
                                                  @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM conversation WHERE user1_id = #{userId} OR user2_id = #{userId}")
    Long countConversations(@Param("userId") Long userId);

    @Select("SELECT * FROM conversation WHERE user1_id = #{u1} AND user2_id = #{u2}")
    ConversationEntity selectByUserIds(@Param("u1") Long user1Id, @Param("u2") Long user2Id);

    @Update("""
        UPDATE conversation
        SET last_message = #{lastMessage},
            last_message_time = #{messageTime},
            unread_count_user1 = unread_count_user1 + CASE WHEN user1_id = #{targetUserId} THEN 1 ELSE 0 END,
            unread_count_user2 = unread_count_user2 + CASE WHEN user2_id = #{targetUserId} THEN 1 ELSE 0 END
        WHERE id = #{conversationId}
        """)
    int touchAndIncrementUnread(@Param("conversationId") Long conversationId,
                                @Param("targetUserId") Long targetUserId,
                                @Param("lastMessage") String lastMessage,
                                @Param("messageTime") LocalDateTime messageTime);

    @Update("""
        UPDATE conversation
        SET unread_count_user1 = CASE
                WHEN user1_id = #{userId}
                THEN (SELECT COUNT(*) FROM message WHERE conversation_id = #{conversationId} AND to_user_id = #{userId} AND is_read = 0)
                ELSE unread_count_user1
            END,
            unread_count_user2 = CASE
                WHEN user2_id = #{userId}
                THEN (SELECT COUNT(*) FROM message WHERE conversation_id = #{conversationId} AND to_user_id = #{userId} AND is_read = 0)
                ELSE unread_count_user2
            END
        WHERE id = #{conversationId}
        """)
    int syncUnreadCount(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
