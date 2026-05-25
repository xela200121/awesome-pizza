package com.awesomepizza.service;

import com.awesomepizza.dto.CreateOrderRequest;
import com.awesomepizza.dto.OrderItemResponse;
import com.awesomepizza.dto.OrderResponse;
import com.awesomepizza.exception.OrderStatusException;
import com.awesomepizza.exception.OrderNotFoundException;
import com.awesomepizza.model.Order;
import com.awesomepizza.model.OrderItem;
import com.awesomepizza.model.OrderStatusEnum;
import com.awesomepizza.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order(generateOrderCode(), request.getCustomerName());

        request.getItems()
                .forEach(itemRequest -> {
                    OrderItem item = new OrderItem(itemRequest.getPizzaName(), itemRequest.getQuantity());
                    order.addItem(item);
                });

        return toResponse(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByCode(String orderCode) {
        Order order = orderRepository.findByCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        return toResponse(order);
    }

    @Override
    @Transactional
    public synchronized OrderResponse takeNextOrder() {
        boolean orderInProgressExists = orderRepository.existsByStatus(OrderStatusEnum.IN_PROGRESS);
        if (orderInProgressExists) {
            throw new OrderStatusException("Cannot take a new order because another order is already in progress");
        }
        Order nextOrder = orderRepository.findFirstByStatusOrderByCreatedAtAsc(OrderStatusEnum.RECEIVED)
                .orElseThrow(() -> new OrderNotFoundException("No order found in status RECEIVED"));
        nextOrder.markAsInProgress();
        Order orderInProgress = orderRepository.save(nextOrder);
        return toResponse(orderInProgress);
    }

    @Override
    @Transactional
    public OrderResponse completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("No order found"));

        if (order.getStatus() != OrderStatusEnum.IN_PROGRESS) {
            throw new OrderStatusException("The order must be in progress");
        }

        order.markAsCompleted();

        Order completedOrder = orderRepository.save(order);

        return toResponse(completedOrder);
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCode(order.getCode());
        response.setStatus(order.getStatus().name());
        response.setCustomerName(order.getCustomerName());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponse> itemResponses = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        response.setItems(itemResponses);

        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setPizzaName(item.getPizzaName());
        response.setQuantity(item.getQuantity());

        return response;
    }

    private String generateOrderCode() {
        return "AW-" + UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
    }
}