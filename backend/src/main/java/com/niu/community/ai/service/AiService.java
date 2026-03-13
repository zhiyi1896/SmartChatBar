package com.niu.community.ai.service;

import com.niu.community.ai.client.AiServiceClient;
import com.niu.community.ai.dto.AiAskRequest;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final AiServiceClient aiServiceClient;

    public AiService(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    public String ask(String query, Long userId) {
        AiAskRequest request = new AiAskRequest();
        request.setQuery(query);
        request.setUserId(String.valueOf(userId));
        return aiServiceClient.ask(request).getAnswer();
    }

    public void clearSession(Long userId) {
        aiServiceClient.clearSession(String.valueOf(userId));
    }
}
