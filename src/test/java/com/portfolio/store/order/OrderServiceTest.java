package com.portfolio.store.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.portfolio.store.exception.BusinessException;
import com.portfolio.store.order.OrderDtos.OrderItemRequest;
import com.portfolio.store.order.OrderDtos.OrderRequest;
import com.portfolio.store.order.OrderDtos.OrderResponse;
import com.portfolio.store.product.Product;
import com.portfolio.store.product.ProductRepository;
import com.portfolio.store.user.User;
import com.portfolio.store.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock OrderRepository orderRepository;
    @Mock ProductRepository productRepository;
    @Mock UserRepository userRepository;
    @InjectMocks OrderService service;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("ana@email.com");

        product = new Product();
        product.setId(1L);
        product.setName("Teclado");
        product.setPrice(new BigDecimal("100.00"));
        product.setStock(10);
    }

    @Test
    void shouldCreateOrderAndDecreaseStock() {
        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = service.create("ana@email.com",
                new OrderRequest(List.of(new OrderItemRequest(1L, 3))));

        assertThat(response.total()).isEqualByComparingTo("300.00");
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(product.getStock()).isEqualTo(7);
    }

    @Test
    void shouldFailWhenStockIsInsufficient() {
        when(userRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> service.create("ana@email.com",
                new OrderRequest(List.of(new OrderItemRequest(1L, 11)))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Estoque insuficiente");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldRestoreStockWhenCancelingPendingOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.PENDING);
        order.setTotal(new BigDecimal("300.00"));
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(3);
        item.setUnitPrice(product.getPrice());
        order.getItems().add(item);
        product.setStock(7);

        when(orderRepository.findByIdAndUserEmail(5L, "ana@email.com")).thenReturn(Optional.of(order));

        OrderResponse response = service.cancel("ana@email.com", 5L);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELED);
        assertThat(product.getStock()).isEqualTo(10);
    }

    @Test
    void shouldNotCancelPaidOrder() {
        Order order = new Order();
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findByIdAndUserEmail(5L, "ana@email.com")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancel("ana@email.com", 5L))
                .isInstanceOf(BusinessException.class);
    }
}
