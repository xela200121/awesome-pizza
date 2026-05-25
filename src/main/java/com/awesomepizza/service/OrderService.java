package com.awesomepizza.service;

import com.awesomepizza.dto.CreateOrderRequest;
import com.awesomepizza.dto.OrderResponse;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderByCode(String orderCode);

    OrderResponse takeNextOrder();

    OrderResponse completeOrder(Long orderId);
}
