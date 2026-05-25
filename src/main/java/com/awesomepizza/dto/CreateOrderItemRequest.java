package com.awesomepizza.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderItemRequest {

    @NotBlank
    private String pizzaName;

    @NotNull
    @Min(1)
    private Integer quantity;
}