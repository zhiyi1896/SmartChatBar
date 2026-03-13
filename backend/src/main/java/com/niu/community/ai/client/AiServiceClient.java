package com.niu.community.ai.client;

import com.niu.community.ai.dto.AiAskRequest;
import com.niu.community.ai.dto.AiAskResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/ai")
public interface AiServiceClient {
    @PostExchange("/ask")
    AiAskResponse ask(@RequestBody AiAskRequest request);

    @DeleteExchange("/session/{userId}")
    void clearSession(@PathVariable String userId);
}
