package com.mounim.gestionproduits.services;

import com.mounim.gestionproduits.entities.Product;
import com.mounim.gestionproduits.exceptions.ProductNotFoundException;
import com.mounim.gestionproduits.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import java.util.List;
import java.util.Set;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;

    @Override
    public Page<Product> search(String keyword, int page, int size, String sort, String direction) {
        String property = Set.of("name", "price").contains(sort) ? sort : "name";
        Sort ordering = Sort.by("desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC, property)
                .and(Sort.by("id"));
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100), ordering);
        String query = keyword == null ? "" : keyword.strip();
        return repository.findByNameContainingIgnoreCase(query, pageable);
    }

    @Override
    public Product getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    @Transactional
    public Product create(Product product) {
        Product entity = new Product();
        copyFields(product, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public Product update(Long id, Product product) {
        Product entity = getById(id);
        copyFields(product, entity);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.delete(getById(id));
    }

    @Override
    public StockSummary summary() {
        return new StockSummary(repository.count(), repository.totalQuantity(),
                repository.countByQuantityLessThanEqual(LOW_STOCK_THRESHOLD), repository.stockValue());
    }

    @Override
    public List<Product> lowStockProducts() {
        return repository.findTop5ByQuantityLessThanEqualOrderByQuantityAscIdAsc(LOW_STOCK_THRESHOLD);
    }

    private void copyFields(Product source, Product target) {
        target.setName(source.getName().strip());
        target.setDescription(source.getDescription() == null ? "" : source.getDescription().strip());
        target.setPrice(source.getPrice());
        target.setQuantity(source.getQuantity());
    }
}
