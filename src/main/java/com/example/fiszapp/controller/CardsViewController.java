package com.example.fiszapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardsViewController {

    @GetMapping
    public String cards(
            @RequestParam(defaultValue = "drafts") String tab,
            Model model
    ) {
        model.addAttribute("pageTitle", "Cards");
        model.addAttribute("activeSection", "cards");
        model.addAttribute("activeTab", tab);
        model.addAttribute("contentTemplate", "content/cards-content");
        return "base";
    }
}
