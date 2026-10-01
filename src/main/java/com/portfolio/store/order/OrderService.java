package com.portfolio.store.order;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.store.exception.BusinessException;
import com.portfolio.store.exception.NotFoundException;
import com.portfolio.store.order.OrderDtos.OrderItemRequest;
import com.portfolio.store.order.OrderDtos.OrderRequest;
import com.portfolio.store.order.OrderDtos.OrderResponse;
import com.portfolio.store.product.Product;
import com.portfolio.store.product.ProductRepository;
import com.portfolio.store.user.User;
import com.portfolio.store.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse create(String email, OrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setCreatedAt(Instant.now());

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new NotFoundException("Produto " + itemRequest.productId() + " não encontrado"));

            if (product.getStock() < itemRequest.quantity()) {
                throw new BusinessException("Estoque insuficiente para o produto " + product.getName());
            }
            product.setStock(product.getStock() - itemRequest.quantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.quantity());
            item.setUnitPrice(product.getPrice());
            order.getItems().add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }
        order.setTotal(total);

        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> list(String email, Pageable pageable) {
        return orderRepository.findByUserEmail(email, pageable).map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse cancel(String email, Long orderId) {
        Order order = orderRepository.findByIdAndUserEmail(orderId, email)
                .orElseThrow(() -> new NotFoundException("Pedido " + orderId + " não encontrado"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Apenas pedidos pendentes podem ser cancelados");
        }
        order.getItems().forEach(i ->
                i.getProduct().setStock(i.getProduct().getStock() + i.getQuantity()));
        order.setStatus(OrderStatus.CANCELED);
        return OrderResponse.from(order);
    }
}
