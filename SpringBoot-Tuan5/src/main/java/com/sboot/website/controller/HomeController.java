package com.sboot.website.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping({"/", "/sboot-website"})
    public String home() {
        return "redirect:/admin/categories";
    }
}