package com.example.fiszapp.dto.openrouter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenRouterRequest(
    String model,
    List<Message> messages,
    @JsonProperty("max_tokens") Integer maxTokens,
    Double temperature
) {
    public record Message(
        String role,
        String content
    ) {}
}
