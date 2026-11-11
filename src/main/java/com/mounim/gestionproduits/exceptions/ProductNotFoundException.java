package com.mounim.gestionproduits.exceptions;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Le produit n° " + id + " est introuvable.");
    }
}
