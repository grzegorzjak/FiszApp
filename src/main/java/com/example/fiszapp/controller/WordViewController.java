package com.example.fiszapp.controller;

import com.example.fiszapp.dto.common.PageResponse;
import com.example.fiszapp.dto.word.WordResponse;
import com.example.fiszapp.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/words")
@RequiredArgsConstructor
public class WordViewController {

    private final WordService wordService;

    @GetMapping
    public String wordsPage(Model model) {
        // TODO: Get userId from session/security context after authentication is implemented
        UUID userId = UUID.fromString("ed848abe-5161-4558-b9ad-1a1742cffbbb");
        
        long freeWordsCount = wordService.countFreeWords(userId);
        model.addAttribute("freeWordsCount", freeWordsCount);
        model.addAttribute("pageTitle", "Words");
        model.addAttribute("activeSection", "words");
        model.addAttribute("contentTemplate", "content/words-content");
        
        return "base";
    }

    @GetMapping("/partial")
    public String wordsListPartial(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) Boolean used,
        @RequestParam(required = false) String search,
        Model model
    ) {
        // TODO: Get userId from session/security context after authentication is implemented
        UUID userId = UUID.fromString("ed848abe-5161-4558-b9ad-1a1742cffbbb");
        
        PageResponse<WordResponse> words = wordService.listWords(
            userId, page, size, sort, used, search
        );
        model.addAttribute("words", words);
        return "fragments/words-list";
    }
}
