package com.portfolio.store.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.store.exception.NotFoundException;
import com.portfolio.store.product.ProductDtos.ProductRequest;
import com.portfolio.store.product.ProductDtos.ProductResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String name, Pageable pageable) {
        Page<Product> page = (name == null || name.isBlank())
                ? repository.findAll(pageable)
                : repository.findByNameContainingIgnoreCase(name, pageable);
        return page.map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return ProductResponse.from(getOrThrow(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return ProductResponse.from(repository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getOrThrow(id);
        apply(product, request);
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(getOrThrow(id));
    }

    private Product getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Produto " + id + " não encontrado"));
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
    }
}
