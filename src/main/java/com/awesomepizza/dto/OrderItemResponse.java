package com.awesomepizza.dto;

import lombok.Data;

@Data
public class OrderItemResponse {

    private Long id;
    private String pizzaName;
    private Integer quantity;
}