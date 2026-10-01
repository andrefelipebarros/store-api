package com.portfolio.store.product;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin("0.0") BigDecimal price,
            @NotNull @Min(0) Integer stock) {
    }

    public record ProductResponse(Long id, String name, String description, BigDecimal price, Integer stock) {
        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStock());
        }
    }
}
