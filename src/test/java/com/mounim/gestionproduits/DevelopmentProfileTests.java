package com.mounim.gestionproduits;

import org.springframework.boot.CommandLineRunner;
import com.mounim.gestionproduits.repositories.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:demo-tests;DB_CLOSE_DELAY=-1",
        "app.seed-data=true"
})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "dev-open"})
@Transactional
class DevelopmentProfileTests {
    @Autowired MockMvc mvc;
    @Autowired ProductRepository repository;
    @Autowired CommandLineRunner seedProducts;

    @Test
    void demoDataIsSeededOnceAndDashboardIsAccessibleWithoutLogin() throws Exception {
        assertThat(repository.count()).isEqualTo(12);
        seedProducts.run();
        assertThat(repository.count()).isEqualTo(12);
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Mode pédagogique")));
        mvc.perform(get("/products/new")).andExpect(status().isOk());
    }

    @Test
    void openProfileStillRequiresCsrfForBusinessForms() throws Exception {
        mvc.perform(post("/products").param("name", "Test").param("price", "10")
                .param("quantity", "1")).andExpect(status().isForbidden());
        mvc.perform(post("/products").with(csrf()).param("name", "Test")
                .param("price", "10").param("quantity", "1"))
                .andExpect(redirectedUrl("/products"));
        assertThat(repository.count()).isEqualTo(13);
    }
}