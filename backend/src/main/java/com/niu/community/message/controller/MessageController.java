package com.niu.community.message.controller;

import com.niu.community.common.model.PageResult;
import com.niu.community.common.model.Result;
import com.niu.community.message.dto.MessageDTO;
import com.niu.community.message.dto.SendMessageDTO;
import com.niu.community.message.service.MessageService;
import com.niu.community.message.vo.ConversationVO;
import com.niu.community.message.vo.MessageVO;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public Result<MessageDTO> send(@RequestBody @Valid SendMessageDTO request,
                                   @RequestAttribute(value = "userId", required = false) Long userId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("发送成功", messageService.sendMessage(userId, request));
    }

    @GetMapping("/conversations")
    public Result<PageResult<ConversationVO>> conversations(@RequestAttribute(value = "userId", required = false) Long userId,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "20") int pageSize) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("获取成功", messageService.getConversations(userId, page, pageSize));
    }

    @GetMapping("/recent/{otherUserId}")
    public Result<List<MessageVO>> recent(@RequestAttribute(value = "userId", required = false) Long userId,
                                          @PathVariable Long otherUserId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("获取成功", messageService.getRecentMessages(userId, otherUserId));
    }

    @GetMapping("/earlier/{otherUserId}")
    public Result<List<MessageVO>> earlier(@RequestAttribute(value = "userId", required = false) Long userId,
                                           @PathVariable Long otherUserId,
                                           @RequestParam String beforeTime) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success("获取成功", messageService.getEarlierMessages(userId, otherUserId, LocalDateTime.parse(beforeTime)));
    }

    @PostMapping("/read/{conversationId}")
    public Result<Void> read(@RequestAttribute(value = "userId", required = false) Long userId,
                             @PathVariable Long conversationId) {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        messageService.markAsRead(conversationId, userId);
        return Result.success("已读成功");
    }
}
