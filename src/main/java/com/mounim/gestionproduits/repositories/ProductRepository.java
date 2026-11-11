package com.mounim.gestionproduits.repositories;

import com.mounim.gestionproduits.entities.Product;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);
    long countByQuantityLessThanEqual(int threshold);
    List<Product> findTop5ByQuantityLessThanEqualOrderByQuantityAscIdAsc(int threshold);

    @Query("select coalesce(sum(p.quantity), 0) from Product p")
    long totalQuantity();

    @Query("select coalesce(sum(p.price * p.quantity), 0.0) from Product p")
    Double stockValue();
}
