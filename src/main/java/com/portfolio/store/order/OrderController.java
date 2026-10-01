package com.portfolio.store.order;

import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.portfolio.store.order.OrderDtos.OrderRequest;
import com.portfolio.store.order.OrderDtos.OrderResponse;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(Principal principal, @Valid @RequestBody OrderRequest request) {
        return service.create(principal.getName(), request);
    }

    @GetMapping
    public Page<OrderResponse> list(Principal principal, Pageable pageable) {
        return service.list(principal.getName(), pageable);
    }

    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(Principal principal, @PathVariable Long id) {
        return service.cancel(principal.getName(), id);
    }
}
