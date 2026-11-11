package com.mounim.gestionproduits.controllers;

import com.mounim.gestionproduits.exceptions.ProductNotFoundException;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@ControllerAdvice
@RequiredArgsConstructor
public class ViewAdvice {
    private final Environment environment;

    @ModelAttribute
    void navigation(Model model, Authentication authentication) {
        boolean open = environment.acceptsProfiles(Profiles.of("dev-open & !mysql"));
        boolean authenticated = authentication != null &&
                authentication.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"));
        boolean admin = authenticated && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("devOpen", open);
        model.addAttribute("canManage", open || admin);
        model.addAttribute("signedIn", authenticated);
        model.addAttribute("displayName", authenticated ? authentication.getName() : "Visiteur");
        model.addAttribute("lowStockThreshold", com.mounim.gestionproduits.services.ProductService.LOW_STOCK_THRESHOLD);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String missing(ProductNotFoundException exception, Model model, Authentication authentication) {
        navigation(model, authentication);
        model.addAttribute("status", 404);
        model.addAttribute("errorTitle", "Produit introuvable");
        model.addAttribute("errorMessage", exception.getMessage());
        return "error";
    }
}
