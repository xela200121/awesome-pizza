package com.awesomepizza;

import com.awesomepizza.dto.CreateOrderItemRequest;
import com.awesomepizza.dto.CreateOrderRequest;
import com.awesomepizza.dto.OrderResponse;
import com.awesomepizza.exception.OrderStatusException;
import com.awesomepizza.exception.OrderNotFoundException;
import com.awesomepizza.model.Order;
import com.awesomepizza.model.OrderItem;
import com.awesomepizza.model.OrderStatusEnum;
import com.awesomepizza.repository.OrderRepository;
import com.awesomepizza.service.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private CreateOrderItemRequest createOrderItemRequest;
    private CreateOrderRequest createOrderRequest;
    private Order order;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        createOrderItemRequest = new CreateOrderItemRequest();
        createOrderItemRequest.setPizzaName("Margherita");
        createOrderItemRequest.setQuantity(2);

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setCustomerName("Mario Rossi");
        createOrderRequest.setItems(List.of(createOrderItemRequest));

        order = new Order();
        order.setId(1L);
        order.setCode("AW-PIZZA-001");
        order.setCustomerName("Mario Rossi");
        order.setStatus(OrderStatusEnum.RECEIVED);
        order.setCreatedAt(LocalDateTime.now().minusMinutes(5));
        order.setUpdatedAt(LocalDateTime.now().minusMinutes(5));

        orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setPizzaName("Margherita");
        orderItem.setQuantity(2);
        orderItem.setOrder(order);

        order.setItems(List.of(orderItem));
    }

    @Test
    void createOrder_shouldCreateOrderWithReceivedStatus() {
        Order savedOrder = order;
        Long orderId = 1L;

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(createOrderRequest);

        assertNotNull(response);
        assertEquals(orderId, response.getId());
        assertEquals("AW-PIZZA-001", response.getCode());
        assertEquals(OrderStatusEnum.RECEIVED.name(), response.getStatus());
        assertEquals(1, response.getItems().size());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_shouldSaveExpectedEntity() {

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.createOrder(createOrderRequest);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        assertNotNull(response);
        assertNotNull(savedOrder.getCode());
        assertTrue(savedOrder.getCode().startsWith("AW-"));
        assertEquals("Mario Rossi", savedOrder.getCustomerName());
        assertEquals(OrderStatusEnum.RECEIVED, savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedAt());
        assertNotNull(savedOrder.getUpdatedAt());
        assertEquals(1, savedOrder.getItems().size());
        assertEquals("Margherita", savedOrder.getItems().getFirst().getPizzaName());
        assertEquals(2, savedOrder.getItems().getFirst().getQuantity());
        assertSame(savedOrder, savedOrder.getItems().getFirst().getOrder());
        assertEquals(savedOrder.getCode(), response.getCode());
    }

    @Test
    void createOrder_shouldCreateOrderWithMultipleItems() {
        CreateOrderItemRequest firstItemRequest = createOrderItemRequest;

        CreateOrderItemRequest secondItemRequest = new CreateOrderItemRequest();
        secondItemRequest.setPizzaName("Diavola");
        secondItemRequest.setQuantity(1);

        createOrderRequest.setItems(List.of(firstItemRequest, secondItemRequest));

        Order savedOrder = order;

        OrderItem firstSavedItem = orderItem;

        OrderItem secondSavedItem = new OrderItem();
        secondSavedItem.setId(2L);
        secondSavedItem.setPizzaName("Diavola");
        secondSavedItem.setQuantity(1);
        secondSavedItem.setOrder(savedOrder);

        savedOrder.setItems(List.of(firstSavedItem, secondSavedItem));

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(createOrderRequest);

        assertNotNull(response);
        assertEquals(2, response.getItems().size());

        assertEquals("Margherita", response.getItems().get(0).getPizzaName());
        assertEquals(2, response.getItems().get(0).getQuantity());

        assertEquals("Diavola", response.getItems().get(1).getPizzaName());
        assertEquals(1, response.getItems().get(1).getQuantity());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getOrderByCode_shouldReturnOrderDetails() {
        String orderCode = "AW-PIZZA-001";

        order.setStatus(OrderStatusEnum.IN_PROGRESS);

        when(orderRepository.findByCode(orderCode)).thenReturn(Optional.of(order));

        OrderResponse response = orderService.getOrderByCode(orderCode);

        assertNotNull(response);
        assertEquals(orderCode, response.getCode());
        assertEquals(OrderStatusEnum.IN_PROGRESS.name(), response.getStatus());
        assertEquals(1, response.getItems().size());

        assertEquals("Margherita", response.getItems().getFirst().getPizzaName());
        assertEquals(2, response.getItems().getFirst().getQuantity());

        verify(orderRepository).findByCode(orderCode);
    }

    @Test
    void getOrderByCode_shouldRejectUnknownCode() {
        String orderCode = "AW-PIZZA-999";

        when(orderRepository.findByCode(orderCode)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.getOrderByCode(orderCode);
        });

        verify(orderRepository).findByCode(orderCode);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void takeNextOrder_shouldMoveOldestReceivedOrderToInProgress() {

        Order oldestOrder = order;

        when(orderRepository.existsByStatus(OrderStatusEnum.IN_PROGRESS))
                .thenReturn(false);

        when(orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatusEnum.RECEIVED))
                .thenReturn(Optional.of(oldestOrder));

        when(orderRepository.save(oldestOrder))
                .thenReturn(oldestOrder);

        OrderResponse response = orderService.takeNextOrder();

        assertNotNull(response);

        assertEquals("AW-PIZZA-001", response.getCode());
        assertEquals(OrderStatusEnum.IN_PROGRESS.name(), response.getStatus());

        assertEquals(1, response.getItems().size());
        assertEquals("Margherita", response.getItems().getFirst().getPizzaName());
        assertEquals(2, response.getItems().getFirst().getQuantity());

        assertEquals(OrderStatusEnum.IN_PROGRESS, oldestOrder.getStatus());
        assertTrue(oldestOrder.getUpdatedAt().isAfter(oldestOrder.getCreatedAt()));
        assertEquals(oldestOrder.getUpdatedAt(), response.getUpdatedAt());

        verify(orderRepository).existsByStatus(OrderStatusEnum.IN_PROGRESS);
        verify(orderRepository).findFirstByStatusOrderByCreatedAtAsc(OrderStatusEnum.RECEIVED);
        verify(orderRepository).save(oldestOrder);
    }

    @Test
    void takeNextOrder_shouldRejectWhenNoReceivedOrdersExist() {

        when(orderRepository.existsByStatus(OrderStatusEnum.IN_PROGRESS))
                .thenReturn(false);

        when(orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatusEnum.RECEIVED))
                .thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.takeNextOrder();
        });

        verify(orderRepository).existsByStatus(OrderStatusEnum.IN_PROGRESS);
        verify(orderRepository).findFirstByStatusOrderByCreatedAtAsc(OrderStatusEnum.RECEIVED);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void takeNextOrder_shouldRejectWhenOrderAlreadyInProgress() {

        when(orderRepository.existsByStatus(OrderStatusEnum.IN_PROGRESS))
                .thenReturn(true);

        assertThrows(OrderStatusException.class, () -> {
            orderService.takeNextOrder();
        });

        verify(orderRepository).existsByStatus(OrderStatusEnum.IN_PROGRESS);
        verify(orderRepository, never())
                .findFirstByStatusOrderByCreatedAtAsc(OrderStatusEnum.RECEIVED);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void completeOrder_shouldMoveOrderToCompleted() {

        Long orderId = 1L;

        Order order = this.order;
        order.setStatus(OrderStatusEnum.IN_PROGRESS);
        LocalDateTime previousUpdatedAt = order.getUpdatedAt();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        OrderResponse response = orderService.completeOrder(orderId);

        assertNotNull(response);

        assertEquals("AW-PIZZA-001", response.getCode());
        assertEquals(OrderStatusEnum.COMPLETED.name(), response.getStatus());

        assertEquals(1, response.getItems().size());
        assertEquals("Margherita", response.getItems().getFirst().getPizzaName());
        assertEquals(2, response.getItems().getFirst().getQuantity());

        assertEquals(OrderStatusEnum.COMPLETED, order.getStatus());
        assertTrue(order.getUpdatedAt().isAfter(previousUpdatedAt));
        assertEquals(order.getUpdatedAt(), response.getUpdatedAt());

        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(order);
    }

    @Test
    void completeOrder_shouldRejectUnknownOrder() {

        Long orderId = 999L;

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.completeOrder(orderId);
        });

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void completeOrder_shouldRejectOrderNotInProgress() {

        Long orderId = 1L;

        Order order = this.order;
        order.setStatus(OrderStatusEnum.RECEIVED);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(OrderStatusException.class, () -> {
            orderService.completeOrder(orderId);
        });

        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

}
