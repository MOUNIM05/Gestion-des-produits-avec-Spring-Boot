package com.mounim.gestionproduits;

import com.mounim.gestionproduits.entities.Product;
import com.mounim.gestionproduits.repositories.ProductRepository;
import com.mounim.gestionproduits.services.ProductService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class GestionProduitsApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired ProductRepository repository;
    @Autowired ProductService service;

    Product product(String name, double price, int quantity) {
        return repository.save(new Product(null, name, "Description", price, quantity));
    }

    @Test
    void anonymousVisitorMustLogIn() throws Exception {
        mvc.perform(get("/products")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Connectez-vous")));
        mvc.perform(get("/css/style.css")).andExpect(status().isOk());
    }

    @Test
    void loginUsesConfiguredPasswordsAndRejectsIncorrectPasswords() throws Exception {
        mvc.perform(formLogin().user("user").password("User-test-2026"))
                .andExpect(authenticated().withUsername("user"))
                .andExpect(redirectedUrl("/"));
        mvc.perform(formLogin().user("admin").password("incorrect"))
                .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void logoutEndsSession() throws Exception {
        mvc.perform(logout()).andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void readerCanConsultButCannotManageProducts() throws Exception {
        Product existing = product("Clavier", 250, 4);
        mvc.perform(get("/products")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Clavier")))
                .andExpect(content().string(not(containsString("/products/new"))))
                .andExpect(content().string(not(containsString("/products/edit/"))));
        mvc.perform(get("/products/new")).andExpect(status().isForbidden());
        mvc.perform(get("/products/edit/" + existing.getId())).andExpect(status().isForbidden());
        mvc.perform(post("/products").with(csrf()).param("name", "Interdit")
                .param("price", "10").param("quantity", "1")).andExpect(status().isForbidden());
        mvc.perform(post("/products/edit/" + existing.getId()).with(csrf()))
                .andExpect(status().isForbidden());
        mvc.perform(post("/products/delete/" + existing.getId()).with(csrf()))
                .andExpect(status().isForbidden());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateUpdateAndDeleteWithoutOverwritingSubmittedId() throws Exception {
        Product original = product("Original", 10, 2);
        mvc.perform(get("/products/new")).andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(post("/products").with(csrf()).param("id", original.getId().toString())
                .param("name", " Nouveau ").param("description", " Test ")
                .param("price", "199.90").param("quantity", "7"))
                .andExpect(redirectedUrl("/products")).andExpect(flash().attributeExists("success"));
        assertThat(repository.count()).isEqualTo(2);
        assertThat(repository.findById(original.getId()).orElseThrow().getName()).isEqualTo("Original");
        Product created = repository.findAll().stream().filter(p -> p.getName().equals("Nouveau"))
                .findFirst().orElseThrow();
        assertThat(created.getDescription()).isEqualTo("Test");
        mvc.perform(get("/products/edit/" + created.getId())).andExpect(status().isOk());
        mvc.perform(post("/products/edit/" + created.getId()).with(csrf())
                .param("id", original.getId().toString()).param("name", "Modifié")
                .param("price", "299.90").param("quantity", "0"))
                .andExpect(redirectedUrl("/products"));
        assertThat(service.getById(created.getId()).getName()).isEqualTo("Modifié");
        assertThat(service.getById(original.getId()).getName()).isEqualTo("Original");
        mvc.perform(post("/products/delete/" + created.getId()).with(csrf()))
                .andExpect(redirectedUrl("/products"));
        assertThat(repository.existsById(created.getId())).isFalse();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void businessFormsRequireCsrf() throws Exception {
        Product existing = product("Clavier", 10, 1);
        mvc.perform(post("/products").param("name", "Test").param("price", "10")
                .param("quantity", "1")).andExpect(status().isForbidden());
        mvc.perform(post("/products/edit/" + existing.getId())).andExpect(status().isForbidden());
        mvc.perform(post("/products/delete/" + existing.getId())).andExpect(status().isForbidden());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidValuesAreShownBesideFieldsWithoutSaving() throws Exception {
        mvc.perform(post("/products").with(csrf()).param("name", " ")
                .param("price", "0").param("quantity", "-1"))
                .andExpect(status().isOk()).andExpect(view().name("product-form"))
                .andExpect(model().attributeHasFieldErrors("product", "name", "price", "quantity"))
                .andExpect(content().string(containsString("Le nom est obligatoire.")));
        assertThat(repository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void malformedNumbersHaveFrenchFieldMessages() throws Exception {
        mvc.perform(post("/products").with(csrf()).param("name", "Clavier")
                .param("price", "abc").param("quantity", "1.5"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("product", "price", "quantity"))
                .andExpect(content().string(containsString("Veuillez saisir un prix valide")))
                .andExpect(content().string(containsString("Veuillez saisir un nombre entier valide")));
        assertThat(repository.count()).isZero();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidUpdateKeepsCorrectFormActionAndOriginalValues() throws Exception {
        Product existing = product("Original", 30, 2);
        mvc.perform(post("/products/edit/" + existing.getId()).with(csrf())
                .param("name", "").param("price", "-1").param("quantity", "2"))
                .andExpect(status().isOk()).andExpect(view().name("product-form"))
                .andExpect(model().attribute("editing", true))
                .andExpect(content().string(containsString("action=\"/products/edit/" + existing.getId() + "\"")));
        assertThat(service.getById(existing.getId()).getName()).isEqualTo("Original");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void missingProductsReturnFriendly404() throws Exception {
        mvc.perform(get("/products/edit/999999"))
                .andExpect(status().isNotFound()).andExpect(view().name("error"))
                .andExpect(content().string(containsString("Produit introuvable")));
        mvc.perform(post("/products/delete/999999").with(csrf())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void searchPaginationAndPriceSortingRenderCorrectResults() throws Exception {
        product("Clavier cher", 300, 3);
        product("clavier abordable", 100, 5);
        product("Souris", 50, 10);
        mvc.perform(get("/products").param("keyword", "CLAVIER").param("size", "1")
                .param("sort", "price").param("direction", "desc"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Clavier cher")))
                .andExpect(content().string(not(containsString("clavier abordable"))))
                .andExpect(content().string(containsString("Page 1 sur 2")));
        mvc.perform(get("/products").param("keyword", "CLAVIER").param("size", "1")
                .param("page", "99").param("sort", "price").param("direction", "desc"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("clavier abordable")))
                .andExpect(content().string(containsString("Page 2 sur 2")));
        mvc.perform(get("/products").param("page", "-1").param("size", "0")
                .param("sort", "invalid").param("direction", "invalid"))
                .andExpect(status().isOk()).andExpect(model().attribute("sort", "name"))
                .andExpect(model().attribute("direction", "asc")).andExpect(model().attribute("size", 1));
    }

    @Test
    @WithMockUser(roles = "USER")
    void emptyCatalogueAndEmptySearchRender() throws Exception {
        mvc.perform(get("/products")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Aucun produit trouvé")));
        product("Clavier", 10, 2);
        mvc.perform(get("/products").param("keyword", "introuvable"))
                .andExpect(status().isOk()).andExpect(content().string(containsString("Aucun produit trouvé")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void productContentIsEscapedInHtml() throws Exception {
        product("<script>alert(1)</script>", 15, 1);
        mvc.perform(get("/products")).andExpect(status().isOk())
                .andExpect(content().string(containsString("&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test
    @WithMockUser(roles = "USER")
    void dashboardCalculatesInventoryAndLowStockIncludingZero() throws Exception {
        product("Rupture", 10, 0);
        product("Faible", 20, 5);
        product("Disponible", 30, 6);
        assertThat(service.summary()).isEqualTo(new ProductService.StockSummary(3, 11, 2, 280));
        assertThat(service.lowStockProducts()).extracting(Product::getName).containsExactly("Rupture", "Faible");
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Rupture de stock")));
    }

    @Test
    void emptyInventoryHasZeroStatisticsAndServiceValidatesInput() {
        assertThat(service.summary()).isEqualTo(new ProductService.StockSummary(0, 0, 0, 0));
        assertThatThrownBy(() -> service.create(new Product(null, "", "", 10.0, 1)))
                .isInstanceOf(ConstraintViolationException.class);
        assertThat(repository.count()).isZero();
    }
}