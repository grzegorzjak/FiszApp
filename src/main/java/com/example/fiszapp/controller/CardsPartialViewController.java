package com.example.fiszapp.controller;

import com.example.fiszapp.dto.card.CardResponse;
import com.example.fiszapp.dto.common.PageResponse;
import com.example.fiszapp.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/cards/partial")
@RequiredArgsConstructor
public class CardsPartialViewController {

    private final CardService cardService;
    private static final UUID TEMP_USER_ID = UUID.fromString("ed848abe-5161-4558-b9ad-1a1742cffbbb");

    @GetMapping("/drafts")
    public String getDrafts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        PageResponse<CardResponse> cards = cardService.listCards(
                TEMP_USER_ID, "DRAFT", page, size, null
        );
        model.addAttribute("cards", cards);
        model.addAttribute("status", "DRAFT");
        return "fragments/cards-list";
    }

    @GetMapping("/accepted")
    public String getAccepted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        PageResponse<CardResponse> cards = cardService.listCards(
                TEMP_USER_ID, "ACCEPTED", page, size, null
        );
        model.addAttribute("cards", cards);
        model.addAttribute("status", "ACCEPTED");
        return "fragments/cards-list";
    }

    @GetMapping("/archived")
    public String getArchived(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        PageResponse<CardResponse> cards = cardService.listCards(
                TEMP_USER_ID, "ARCHIVED", page, size, null
        );
        model.addAttribute("cards", cards);
        model.addAttribute("status", "ARCHIVED");
        return "fragments/cards-list";
    }
}
