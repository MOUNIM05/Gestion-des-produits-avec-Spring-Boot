package com.mounim.gestionproduits.services;

import com.mounim.gestionproduits.entities.Product;
import org.springframework.data.domain.Page;
import jakarta.validation.Valid;
import java.util.List;

public interface ProductService {
    int LOW_STOCK_THRESHOLD = 5;
    Page<Product> search(String keyword, int page, int size, String sort, String direction);
    Product getById(Long id);
    Product create(@Valid Product product);
    Product update(Long id, @Valid Product product);
    void delete(Long id);
    StockSummary summary();
    List<Product> lowStockProducts();

    record StockSummary(long productCount, long totalQuantity, long lowStockCount, double stockValue) {}
}
