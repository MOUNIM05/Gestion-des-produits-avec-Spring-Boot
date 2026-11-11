package com.mounim.gestionproduits.controllers;

import com.mounim.gestionproduits.entities.Product;
import com.mounim.gestionproduits.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;

    @InitBinder("product")
    void bindProduct(WebDataBinder binder) {
        binder.setAllowedFields("name", "description", "price", "quantity");
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(defaultValue = "name") String sort,
                       @RequestParam(defaultValue = "asc") String direction, Model model) {
        String safeSort = "price".equals(sort) ? "price" : "name";
        String safeDirection = "desc".equalsIgnoreCase(direction) ? "desc" : "asc";
        var products = service.search(keyword, page, size, safeSort, safeDirection);
        if (products.getTotalPages() > 0 && products.getNumber() >= products.getTotalPages()) {
            products = service.search(keyword, products.getTotalPages() - 1, size, safeSort, safeDirection);
        }
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sort", safeSort);
        model.addAttribute("direction", safeDirection);
        model.addAttribute("size", products.getSize());
        model.addAttribute("pageSizes", java.util.List.of(10, 20, 50));
        model.addAttribute("pageStart", Math.max(0, products.getNumber() - 2));
        model.addAttribute("pageEnd", Math.min(products.getTotalPages() - 1, products.getNumber() + 2));
        return "products";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product(null, "", "", null, 0));
        model.addAttribute("editing", false);
        return "product-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("product") Product product, BindingResult errors,
                         Model model, RedirectAttributes redirect) {
        if (errors.hasErrors()) {
            model.addAttribute("editing", false);
            return "product-form";
        }
        service.create(product);
        redirect.addFlashAttribute("success", "Le produit a été ajouté avec succès.");
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("product", service.getById(id));
        model.addAttribute("editing", true);
        return "product-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("product") Product product,
                         BindingResult errors, Model model, RedirectAttributes redirect) {
        service.getById(id);
        if (errors.hasErrors()) {
            product.setId(id);
            model.addAttribute("editing", true);
            return "product-form";
        }
        service.update(id, product);
        redirect.addFlashAttribute("success", "Les modifications ont été enregistrées.");
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        service.delete(id);
        redirect.addFlashAttribute("success", "Le produit a été supprimé.");
        return "redirect:/products";
    }
}
