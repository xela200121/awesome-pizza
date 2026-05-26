package com.awesomepizza.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import com.awesomepizza.dto.CreateOrderRequest;
import com.awesomepizza.dto.OrderResponse;
import com.awesomepizza.service.OrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create an order",
            description = "Creates a new order from a customer name and one or more pizza items.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created"),
            @ApiResponse(responseCode = "400", description = "Invalid or malformed order request")
    })
    @Tag(name = "User")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{code}")
    @Operation(summary = "Get an order by code", description = "Retrieves the order matching the public order code.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @Tag(name = "User")
    public ResponseEntity<OrderResponse> getOrderByCode(
            @Parameter(description = "Public order code", example = "AW-12345678")
            @PathVariable String code) {
        OrderResponse response = orderService.getOrderByCode(code);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/take-next")
    @Operation(summary = "Take the next order",
            description = "Assigns the oldest received order and marks it as in progress.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order assigned"),
            @ApiResponse(responseCode = "404", description = "No order available found"),
            @ApiResponse(responseCode = "409", description = "Another order is already in progress")
    })
    @Tag(name = "Pizza Chef")
    public ResponseEntity<OrderResponse> takeNextOrder() {
        OrderResponse response = orderService.takeNextOrder();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Complete an order", description = "Marks an in-progress order as completed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order completed"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Order is not in progress")
    })
    @Tag(name = "Pizza Chef")
    public ResponseEntity<OrderResponse> completeOrder(
            @Parameter(description = "Order id")
            @PathVariable Long id) {
        OrderResponse response = orderService.completeOrder(id);
        return ResponseEntity.ok(response);
    }
}
