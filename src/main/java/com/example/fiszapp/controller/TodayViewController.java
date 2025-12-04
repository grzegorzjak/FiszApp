package com.example.fiszapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class TodayViewController {

    @GetMapping({"/", "/today"})
    public String today(Model model) {
        model.addAttribute("pageTitle", "Today");
        model.addAttribute("activeSection", "today");
        model.addAttribute("contentTemplate", "content/today-content");
        return "base";
    }
}
