package com.niu.community.message.service;

import com.niu.community.common.model.PageResult;
import com.niu.community.message.dto.MessageDTO;
import com.niu.community.message.dto.SendMessageDTO;
import com.niu.community.message.entity.ConversationEntity;
import com.niu.community.message.entity.MessageEntity;
import com.niu.community.message.mapper.ConversationMapper;
import com.niu.community.message.mapper.MessageMapper;
import com.niu.community.message.vo.ConversationVO;
import com.niu.community.message.vo.MessageVO;
import com.niu.community.user.entity.UserEntity;
import com.niu.community.user.mapper.UserMapper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageMapper messageMapper;
    private final ConversationMapper conversationMapper;
    private final UserMapper userMapper;

    public MessageService(MessageMapper messageMapper, ConversationMapper conversationMapper, UserMapper userMapper) {
        this.messageMapper = messageMapper;
        this.conversationMapper = conversationMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public MessageDTO sendMessage(Long fromUserId, SendMessageDTO request) {
        ConversationEntity conversation = getOrCreateConversation(fromUserId, request.getToUserId());
        LocalDateTime now = LocalDateTime.now();
        MessageEntity message = new MessageEntity();
        message.setConversationId(conversation.getId());
        message.setFromUserId(fromUserId);
        message.setToUserId(request.getToUserId());
        message.setContent(request.getContent());
        message.setIsRead(0);
        message.setCreateTime(now);
        messageMapper.insert(message);

        String lastMessage = request.getContent().length() > 50 ? request.getContent().substring(0, 50) + "..." : request.getContent();
        conversationMapper.touchAndIncrementUnread(conversation.getId(), request.getToUserId(), lastMessage, now);
        return toDTO(message);
    }

    public PageResult<ConversationVO> getConversations(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Map<String, Object>> rows = conversationMapper.selectConversations(userId, offset, pageSize);
        List<ConversationVO> list = rows.stream().map(this::toConversationVO).collect(Collectors.toList());
        long total = conversationMapper.countConversations(userId);
        return new PageResult<>(list, total, page, pageSize);
    }

    public List<MessageVO> getRecentMessages(Long userId, Long otherUserId) {
        ConversationEntity conversation = conversationMapper.selectByUserIds(Math.min(userId, otherUserId), Math.max(userId, otherUserId));
        if (conversation == null) {
            return List.of();
        }
        return messageMapper.selectRecentMessages(conversation.getId()).stream().map(message -> toMessageVO(message, userId)).toList();
    }

    public List<MessageVO> getEarlierMessages(Long userId, Long otherUserId, LocalDateTime beforeTime) {
        ConversationEntity conversation = conversationMapper.selectByUserIds(Math.min(userId, otherUserId), Math.max(userId, otherUserId));
        if (conversation == null) {
            return List.of();
        }
        return messageMapper.selectEarlierMessages(conversation.getId(), beforeTime).stream().map(message -> toMessageVO(message, userId)).toList();
    }

    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        ConversationEntity conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            return;
        }
        messageMapper.markAsRead(conversationId, userId);
        conversationMapper.syncUnreadCount(conversationId, userId);
    }

    public ConversationEntity getConversationById(Long id) {
        return conversationMapper.selectById(id);
    }

    private ConversationEntity getOrCreateConversation(Long user1Id, Long user2Id) {
        Long u1 = Math.min(user1Id, user2Id);
        Long u2 = Math.max(user1Id, user2Id);
        ConversationEntity conversation = conversationMapper.selectByUserIds(u1, u2);
        if (conversation == null) {
            conversation = new ConversationEntity();
            conversation.setUser1Id(u1);
            conversation.setUser2Id(u2);
            conversation.setUnreadCountUser1(0);
            conversation.setUnreadCountUser2(0);
            conversation.setLastMessageTime(LocalDateTime.now());
            try {
                conversationMapper.insert(conversation);
            } catch (DuplicateKeyException ex) {
                conversation = conversationMapper.selectByUserIds(u1, u2);
            }
        }
        return conversation;
    }

    private MessageDTO toDTO(MessageEntity message) {
        UserEntity user = userMapper.selectById(message.getFromUserId());
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversationId());
        dto.setFromUserId(message.getFromUserId());
        dto.setToUserId(message.getToUserId());
        dto.setContent(message.getContent());
        dto.setIsRead(message.getIsRead());
        dto.setCreateTime(message.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dto.setFromUserNickname(user == null ? "Anonymous" : user.getNickname());
        dto.setFromUserAvatar(user == null ? null : user.getAvatar());
        return dto;
    }

    private ConversationVO toConversationVO(Map<String, Object> row) {
        ConversationVO vo = new ConversationVO();
        vo.setConversationId(((Number) row.get("conversationId")).longValue());
        vo.setUserId(((Number) row.get("userId")).longValue());
        vo.setUserName((String) row.get("userName"));
        vo.setUserAvatar((String) row.get("userAvatar"));
        vo.setLastMessage((String) row.get("lastMessage"));
        vo.setUnreadCount(((Number) row.get("unreadCount")).intValue());
        LocalDateTime time = (LocalDateTime) row.get("lastMessageTime");
        vo.setLastMessageTime(time == null ? "" : time.format(DateTimeFormatter.ofPattern("MM-dd HH:mm")));
        return vo;
    }

    private MessageVO toMessageVO(MessageEntity message, Long currentUserId) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setFromUserId(message.getFromUserId());
        vo.setToUserId(message.getToUserId());
        vo.setContent(message.getContent());
        vo.setSelf(currentUserId.equals(message.getFromUserId()));
        vo.setRead(message.getIsRead() == 1);
        LocalDateTime time = message.getCreateTime();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        if (time.toLocalDate().equals(today)) {
            vo.setTime("Today " + time.format(DateTimeFormatter.ofPattern("HH:mm")));
        } else if (time.toLocalDate().equals(yesterday)) {
            vo.setTime("Yesterday " + time.format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            vo.setTime(time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        return vo;
    }
}
