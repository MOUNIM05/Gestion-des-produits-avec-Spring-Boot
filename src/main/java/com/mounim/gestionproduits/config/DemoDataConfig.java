package com.mounim.gestionproduits.config;

import com.mounim.gestionproduits.entities.Product;
import com.mounim.gestionproduits.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import java.util.List;

@Configuration
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DemoDataConfig {
    @Bean
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.saveAll(List.of(
                    new Product(null, "MacBook Air M3", "Ordinateur portable 13 pouces, 16 Go de mémoire.", 12990.0, 12),
                    new Product(null, "Écran Dell 27 pouces", "Moniteur QHD pour un espace de travail confortable.", 2890.0, 8),
                    new Product(null, "Clavier mécanique", "Clavier compact AZERTY avec connexion USB-C.", 790.0, 4),
                    new Product(null, "Souris sans fil", "Souris ergonomique rechargeable.", 349.0, 32),
                    new Product(null, "Casque audio", "Casque sans fil avec réduction de bruit.", 1490.0, 3),
                    new Product(null, "Hub USB-C", "Adaptateur 7 ports : HDMI, USB et lecteur de cartes.", 459.0, 18),
                    new Product(null, "Webcam HD", "Caméra 1080p pour visioconférence.", 599.0, 0),
                    new Product(null, "Support ordinateur", "Support réglable en aluminium.", 299.0, 21),
                    new Product(null, "SSD externe 1 To", "Stockage portable USB-C haute vitesse.", 1090.0, 5),
                    new Product(null, "Tapis de bureau", "Surface de travail antidérapante, grand format.", 199.0, 40),
                    new Product(null, "Lampe de bureau", "Éclairage LED avec intensité réglable.", 389.0, 14),
                    new Product(null, "Câble USB-C", "Câble tressé de 2 mètres, charge rapide.", 129.0, 50)
                ));
            }
        };
    }
}
