package com.mounim.gestionproduits.security;

import jakarta.servlet.DispatcherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import java.util.UUID;

@Configuration
public class SecurityConfig {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users(PasswordEncoder encoder,
                            @Value("${app.demo.user-password:}") String userPassword,
                            @Value("${app.demo.admin-password:}") String adminPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername("user").password(encoder.encode(demoPassword("user", userPassword)))
                        .roles("USER").build(),
                User.withUsername("admin").password(encoder.encode(demoPassword("admin", adminPassword)))
                        .roles("USER", "ADMIN").build());
    }

    private String demoPassword(String username, String configured) {
        if (!configured.isBlank()) {
            return configured;
        }
        String generated = UUID.randomUUID().toString();
        LOG.warn("Compte de démonstration {} — mot de passe temporaire : {}", username, generated);
        return generated;
    }

    @Bean
    @Profile("!dev-open | mysql")
    SecurityFilterChain secured(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                .requestMatchers("/css/**", "/js/**", "/login", "/error", "/access-denied").permitAll()
                .requestMatchers("/products/new", "/products/edit/**", "/products/delete/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/products").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/", "/products").hasAnyRole("USER", "ADMIN")
                .anyRequest().denyAll())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
            .exceptionHandling(errors -> errors.accessDeniedPage("/access-denied"));
        return http.build();
    }

    @Bean
    @Profile("dev-open & !mysql")
    SecurityFilterChain developmentOnly(HttpSecurity http) throws Exception {
        // Exception limitée à la console H2 : tous les formulaires métier gardent CSRF.
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
