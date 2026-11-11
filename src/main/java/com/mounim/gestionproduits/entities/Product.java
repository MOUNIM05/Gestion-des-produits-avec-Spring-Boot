package com.mounim.gestionproduits.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 120, message = "Le nom ne doit pas dépasser 120 caractères.")
    @Column(nullable = false, length = 120)
    private String name;

    @Size(max = 2000, message = "La description ne doit pas dépasser 2 000 caractères.")
    @Column(length = 2000)
    private String description;

    @NotNull(message = "Le prix est obligatoire.")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être strictement positif.")
    @DecimalMax(value = "999999999.99", message = "Le prix ne doit pas dépasser 999 999 999,99.")
    @Column(nullable = false)
    private Double price;

    @NotNull(message = "La quantité est obligatoire.")
    @Min(value = 0, message = "La quantité doit être supérieure ou égale à zéro.")
    @Column(nullable = false)
    private Integer quantity;
}
