package com.example.fiszapp.dto.openrouter;

import java.util.List;

public record OpenRouterResponse(
    String id,
    String object,
    Long created,
    String model,
    List<Choice> choices,
    Usage usage
) {
    public record Choice(
        Integer index,
        Message message,
        String finishReason
    ) {}
    
    public record Message(
        String role,
        String content
    ) {}
    
    public record Usage(
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
    ) {}
}
