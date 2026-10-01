package com.portfolio.store.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderItemRequest(@NotNull Long productId, @NotNull @Min(1) Integer quantity) {
    }

    public record OrderRequest(@NotEmpty @Valid List<OrderItemRequest> items) {
    }

    public record OrderItemResponse(Long productId, String productName, Integer quantity, BigDecimal unitPrice) {
    }

    public record OrderResponse(Long id, OrderStatus status, BigDecimal total, Instant createdAt,
                                List<OrderItemResponse> items) {
        public static OrderResponse from(Order order) {
            List<OrderItemResponse> items = order.getItems().stream()
                    .map(i -> new OrderItemResponse(i.getProduct().getId(), i.getProduct().getName(),
                            i.getQuantity(), i.getUnitPrice()))
                    .toList();
            return new OrderResponse(order.getId(), order.getStatus(), order.getTotal(), order.getCreatedAt(), items);
        }
    }
}
