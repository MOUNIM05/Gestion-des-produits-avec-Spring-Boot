package com.mounim.gestionproduits.controllers;

import com.mounim.gestionproduits.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final ProductService service;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("stats", service.summary());
        model.addAttribute("lowStockProducts", service.lowStockProducts());
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String denied() {
        return "access-denied";
    }
}
